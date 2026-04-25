package tn.ecocycle.ecocycletn.controllers;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.ecocycle.ecocycletn.dto.ProfileResponse;
import tn.ecocycle.ecocycletn.dto.ProfileUpdateRequest;
import tn.ecocycle.ecocycletn.services.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ProfileResponse getCurrentProfile(@AuthenticationPrincipal String email) {
        return userService.getCurrentProfile(email);
    }

    @PutMapping("/me")
    public ProfileResponse updateCurrentProfile(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        return userService.updateCurrentProfile(email, request);
    }
}
