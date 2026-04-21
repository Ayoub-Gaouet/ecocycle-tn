package tn.ecocycle.ecocycletn.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tn.ecocycle.ecocycletn.dto.AuthResponse;
import tn.ecocycle.ecocycletn.dto.RegisterRequest;
import tn.ecocycle.ecocycletn.dto.UserResponse;
import tn.ecocycle.ecocycletn.exceptions.EmailAlreadyUsedException;
import tn.ecocycle.ecocycletn.exceptions.InvalidCredentialsException;
import tn.ecocycle.ecocycletn.exceptions.GlobalExceptionHandler;
import tn.ecocycle.ecocycletn.security.JwtService;
import tn.ecocycle.ecocycletn.services.AuthService;
import tn.ecocycle.ecocycletn.entities.Role;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void registerCreatesUser() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "citizen@example.com",
                "secret1",
                "Tunis Citizen"
        );
        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(new UserResponse(1L, "citizen@example.com", "Tunis Citizen", Role.USER));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("citizen@example.com"))
                .andExpect(jsonPath("$.fullName").value("Tunis Citizen"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void registerRejectsDuplicateEmailWithBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "TAKEN@example.com",
                "secret1",
                "Other User"
        );
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new EmailAlreadyUsedException("taken@example.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already used: taken@example.com"));
    }

    @Test
    void registerRejectsInvalidPayloadWithBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "not-an-email",
                "123",
                ""
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.email").exists())
                .andExpect(jsonPath("$.validationErrors.password").exists())
                .andExpect(jsonPath("$.validationErrors.fullName").exists());
    }

    @Test
    void loginReturnsJwtResponse() throws Exception {
        when(authService.login(any()))
                .thenReturn(new AuthResponse("jwt-token", "Bearer", 3600, "login@example.com", Role.USER));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "login@example.com",
                                  "password": "secret1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.email").value("login@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void loginRejectsInvalidCredentialsWithUnauthorized() throws Exception {
        when(authService.login(any())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "login@example.com",
                                  "password": "bad-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }
}
