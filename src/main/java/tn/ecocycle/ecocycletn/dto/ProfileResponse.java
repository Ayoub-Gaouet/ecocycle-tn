package tn.ecocycle.ecocycletn.dto;

import tn.ecocycle.ecocycletn.entities.User;

public record ProfileResponse(
        String fullName,
        String email,
        String phone,
        String governorate,
        int ecoPoints
) {

    public static ProfileResponse from(User user) {
        return new ProfileResponse(
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getGovernorate(),
                user.getEcoPoints()
        );
    }
}
