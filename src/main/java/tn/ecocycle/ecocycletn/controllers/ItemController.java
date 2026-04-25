package tn.ecocycle.ecocycletn.controllers;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.ecocycle.ecocycletn.dto.ItemRequest;
import tn.ecocycle.ecocycletn.dto.ItemResponse;
import tn.ecocycle.ecocycletn.entities.Category;
import tn.ecocycle.ecocycletn.services.ItemService;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ResponseEntity<ItemResponse> createItem(
            @AuthenticationPrincipal String ownerEmail,
            @Valid @RequestBody ItemRequest request
    ) {
        ItemResponse response = itemService.createItem(ownerEmail, request);
        return ResponseEntity
                .created(URI.create("/api/items/" + response.id()))
                .body(response);
    }

    @GetMapping
    public List<ItemResponse> listItems(@RequestParam(required = false) Category category) {
        return itemService.listItems(category);
    }

    @GetMapping("/{id}")
    public ItemResponse getItem(@PathVariable Long id) {
        return itemService.getItem(id);
    }

    @PutMapping("/{id}")
    public ItemResponse updateItem(
            @PathVariable Long id,
            @AuthenticationPrincipal String ownerEmail,
            @Valid @RequestBody ItemRequest request
    ) {
        return itemService.updateItem(id, ownerEmail, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long id,
            @AuthenticationPrincipal String ownerEmail
    ) {
        itemService.deleteItem(id, ownerEmail);
        return ResponseEntity.noContent().build();
    }
}
