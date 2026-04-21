package tn.ecocycle.ecocycletn.dto;

import tn.ecocycle.ecocycletn.entities.Role;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresIn,
        String email,
        Role role
) {
}
