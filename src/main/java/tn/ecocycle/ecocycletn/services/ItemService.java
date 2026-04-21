package tn.ecocycle.ecocycletn.services;

import java.util.List;
import tn.ecocycle.ecocycletn.dto.ItemRequest;
import tn.ecocycle.ecocycletn.dto.ItemResponse;
import tn.ecocycle.ecocycletn.entities.Category;

public interface ItemService {

    ItemResponse createItem(String ownerEmail, ItemRequest request);

    List<ItemResponse> listItems(Category category);

    ItemResponse getItem(Long id);

    ItemResponse updateItem(Long id, String ownerEmail, ItemRequest request);

    void deleteItem(Long id, String ownerEmail);
}
