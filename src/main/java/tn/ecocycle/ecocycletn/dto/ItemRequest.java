package tn.ecocycle.ecocycletn.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import tn.ecocycle.ecocycletn.entities.Category;

public record ItemRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 120, message = "Title must be at most 120 characters")
        String title,

        @NotBlank(message = "Description is required")
        @Size(max = 1000, message = "Description must be at most 1000 characters")
        String description,

        @NotNull(message = "Category is required")
        Category category,

        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0.01", message = "Quantity must be greater than 0")
        BigDecimal quantityKg,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.00", message = "Price must be positive or zero")
        BigDecimal priceTnd,

        @NotBlank(message = "Location is required")
        @Size(max = 180, message = "Location must be at most 180 characters")
        String location
) {
}
