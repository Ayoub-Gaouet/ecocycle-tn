package tn.ecocycle.ecocycletn.dto;

import tn.ecocycle.ecocycletn.entities.Role;
import tn.ecocycle.ecocycletn.entities.User;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        Role role
) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole());
    }
}
