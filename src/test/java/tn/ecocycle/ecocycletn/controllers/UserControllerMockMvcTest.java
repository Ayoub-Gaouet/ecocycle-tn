package tn.ecocycle.ecocycletn.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tn.ecocycle.ecocycletn.dto.ProfileResponse;
import tn.ecocycle.ecocycletn.dto.ProfileUpdateRequest;
import tn.ecocycle.ecocycletn.entities.Role;
import tn.ecocycle.ecocycletn.entities.User;
import tn.ecocycle.ecocycletn.exceptions.GlobalExceptionHandler;
import tn.ecocycle.ecocycletn.repositories.UserRepository;
import tn.ecocycle.ecocycletn.security.JwtAuthFilter;
import tn.ecocycle.ecocycletn.security.JwtProperties;
import tn.ecocycle.ecocycletn.security.JwtService;
import tn.ecocycle.ecocycletn.security.SecurityConfig;
import tn.ecocycle.ecocycletn.services.UserService;

@WebMvcTest(UserController.class)
@Import({
        GlobalExceptionHandler.class,
        SecurityConfig.class,
        JwtAuthFilter.class,
        JwtService.class
})
@EnableConfigurationProperties(JwtProperties.class)
@TestPropertySource(properties = {
        "app.security.jwt.secret=profile-test-secret-with-at-least-32-bytes",
        "app.security.jwt.expiration=PT1H"
})
class UserControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void getCurrentProfileReturnsProfileWithValidJwt() throws Exception {
        when(userService.getCurrentProfile("profile@example.com"))
                .thenReturn(new ProfileResponse(
                        "Profile User",
                        "profile@example.com",
                        "+21650000000",
                        "Tunis",
                        25
                ));

        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("profile@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Profile User"))
                .andExpect(jsonPath("$.email").value("profile@example.com"))
                .andExpect(jsonPath("$.phone").value("+21650000000"))
                .andExpect(jsonPath("$.governorate").value("Tunis"))
                .andExpect(jsonPath("$.ecoPoints").value(25))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void updateCurrentProfileUpdatesEditableFieldsOnly() throws Exception {
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "Updated User",
                "+21651111111",
                "Ariana"
        );
        when(userService.updateCurrentProfile(eq("profile@example.com"), any(ProfileUpdateRequest.class)))
                .thenReturn(new ProfileResponse(
                        "Updated User",
                        "profile@example.com",
                        "+21651111111",
                        "Ariana",
                        25
                ));

        mockMvc.perform(put("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("profile@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated User"))
                .andExpect(jsonPath("$.email").value("profile@example.com"))
                .andExpect(jsonPath("$.phone").value("+21651111111"))
                .andExpect(jsonPath("$.governorate").value("Ariana"))
                .andExpect(jsonPath("$.ecoPoints").value(25));
    }

    @Test
    void getCurrentProfileRejectsMissingJwt() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCurrentProfileRejectsInvalidJwt() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid JWT token"));
    }

    private String bearerToken(String email) {
        User user = new User(
                email,
                "hashed-password",
                "Profile User",
                Role.USER
        );
        return "Bearer " + jwtService.generateToken(user);
    }
}
