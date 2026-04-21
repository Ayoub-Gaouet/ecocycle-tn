package tn.ecocycle.ecocycletn.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.ecocycle.ecocycletn.dto.ProfileResponse;
import tn.ecocycle.ecocycletn.dto.ProfileUpdateRequest;
import tn.ecocycle.ecocycletn.entities.Role;
import tn.ecocycle.ecocycletn.entities.User;
import tn.ecocycle.ecocycletn.exceptions.ResourceNotFoundException;
import tn.ecocycle.ecocycletn.repositories.UserRepository;
import tn.ecocycle.ecocycletn.services.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void getCurrentProfileReturnsAuthenticatedUserProfile() {
        User user = new User(
                "profile@example.com",
                "hashed-password",
                "Profile User",
                "+21650000000",
                "Tunis",
                35,
                Role.USER
        );
        when(userRepository.findByEmail("profile@example.com")).thenReturn(Optional.of(user));

        ProfileResponse response = userService.getCurrentProfile("profile@example.com");

        assertThat(response.email()).isEqualTo("profile@example.com");
        assertThat(response.fullName()).isEqualTo("Profile User");
        assertThat(response.phone()).isEqualTo("+21650000000");
        assertThat(response.governorate()).isEqualTo("Tunis");
        assertThat(response.ecoPoints()).isEqualTo(35);
    }

    @Test
    void updateCurrentProfileDoesNotChangeEmailOrEcoPoints() {
        User user = new User(
                "profile@example.com",
                "hashed-password",
                "Old Name",
                "+21650000000",
                "Tunis",
                42,
                Role.USER
        );
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                " New Name ",
                " +21651111111 ",
                " Ariana "
        );
        when(userRepository.findByEmail("profile@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProfileResponse response = userService.updateCurrentProfile("profile@example.com", request);

        assertThat(response.email()).isEqualTo("profile@example.com");
        assertThat(response.fullName()).isEqualTo("New Name");
        assertThat(response.phone()).isEqualTo("+21651111111");
        assertThat(response.governorate()).isEqualTo("Ariana");
        assertThat(response.ecoPoints()).isEqualTo(42);
        verify(userRepository).save(user);
    }

    @Test
    void getCurrentProfileFailsWhenUserDoesNotExist() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentProfile("missing@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found: missing@example.com");
    }
}
