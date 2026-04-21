package tn.ecocycle.ecocycletn.dto;

import java.math.BigDecimal;
import tn.ecocycle.ecocycletn.entities.Category;
import tn.ecocycle.ecocycletn.entities.RecyclableItem;

public record ItemResponse(
        Long id,
        String title,
        String description,
        Category category,
        BigDecimal quantityKg,
        BigDecimal priceTnd,
        String location,
        String ownerEmail,
        String ownerFullName
) {

    public static ItemResponse from(RecyclableItem item) {
        return new ItemResponse(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getCategory(),
                item.getQuantityKg(),
                item.getPriceTnd(),
                item.getLocation(),
                item.getOwner().getEmail(),
                item.getOwner().getFullName()
        );
    }
}
