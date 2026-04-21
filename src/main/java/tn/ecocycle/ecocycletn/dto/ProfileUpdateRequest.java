package tn.ecocycle.ecocycletn.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 120, message = "Full name must be at most 120 characters")
        String fullName,

        @NotBlank(message = "Phone is required")
        @Size(max = 30, message = "Phone must be at most 30 characters")
        String phone,

        @NotBlank(message = "Governorate is required")
        @Size(max = 80, message = "Governorate must be at most 80 characters")
        String governorate
) {
}
