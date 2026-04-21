package tn.ecocycle.ecocycletn.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import tn.ecocycle.ecocycletn.dto.AuthResponse;
import tn.ecocycle.ecocycletn.dto.LoginRequest;
import tn.ecocycle.ecocycletn.dto.RegisterRequest;
import tn.ecocycle.ecocycletn.exceptions.EmailAlreadyUsedException;
import tn.ecocycle.ecocycletn.exceptions.InvalidCredentialsException;
import tn.ecocycle.ecocycletn.security.JwtService;
import tn.ecocycle.ecocycletn.entities.Role;
import tn.ecocycle.ecocycletn.entities.User;
import tn.ecocycle.ecocycletn.repositories.UserRepository;
import tn.ecocycle.ecocycletn.services.impl.AuthServiceImpl;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void registerHashesPasswordWithBCrypt() {
        AuthService service = new AuthServiceImpl(userRepository, new BCryptPasswordEncoder(), jwtService);
        RegisterRequest request = new RegisterRequest(
                "Citizen@Example.com",
                "secret1",
                "Tunis Citizen"
        );
        when(userRepository.existsByEmail("citizen@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("citizen@example.com");
        assertThat(savedUser.getPassword()).isNotEqualTo("secret1");
        assertThat(new BCryptPasswordEncoder().matches("secret1", savedUser.getPassword())).isTrue();
        assertThat(savedUser.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void registerRejectsEmailAlreadyUsed() {
        RegisterRequest request = new RegisterRequest(
                "Taken@Example.com",
                "secret1",
                "Taken User"
        );
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyUsedException.class)
                .hasMessage("Email already used: taken@example.com");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginReturnsJwtWhenPasswordMatches() {
        User user = new User(
                "login@example.com",
                "hashed-password",
                "Login User",
                Role.USER
        );
        when(userRepository.findByEmail("login@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret1", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        AuthResponse response = authService.login(new LoginRequest("login@example.com", "secret1"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600);
        assertThat(response.email()).isEqualTo("login@example.com");
        assertThat(response.role()).isEqualTo(Role.USER);
    }

    @Test
    void loginRejectsInvalidPassword() {
        User user = new User(
                "login@example.com",
                "hashed-password",
                "Login User",
                Role.USER
        );
        when(userRepository.findByEmail("login@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad-password", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("login@example.com", "bad-password")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");
    }
}
