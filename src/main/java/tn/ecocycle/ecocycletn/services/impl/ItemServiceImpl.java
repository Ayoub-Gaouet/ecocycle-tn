package tn.ecocycle.ecocycletn.services.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.ecocycle.ecocycletn.dto.ItemRequest;
import tn.ecocycle.ecocycletn.dto.ItemResponse;
import tn.ecocycle.ecocycletn.entities.Category;
import tn.ecocycle.ecocycletn.entities.RecyclableItem;
import tn.ecocycle.ecocycletn.entities.User;
import tn.ecocycle.ecocycletn.exceptions.ForbiddenOperationException;
import tn.ecocycle.ecocycletn.exceptions.ResourceNotFoundException;
import tn.ecocycle.ecocycletn.repositories.ItemRepository;
import tn.ecocycle.ecocycletn.repositories.UserRepository;
import tn.ecocycle.ecocycletn.services.ItemService;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ItemResponse createItem(String ownerEmail, ItemRequest request) {
        User owner = findUserByEmail(ownerEmail);
        RecyclableItem item = new RecyclableItem(
                request.title().trim(),
                request.description().trim(),
                request.category(),
                request.quantityKg(),
                request.priceTnd(),
                request.location().trim(),
                owner
        );

        return ItemResponse.from(itemRepository.save(item));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> listItems(Category category) {
        List<RecyclableItem> items = category == null
                ? itemRepository.findAll()
                : itemRepository.findByCategory(category);

        return items.stream()
                .map(ItemResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponse getItem(Long id) {
        return ItemResponse.from(findItemById(id));
    }

    @Override
    @Transactional
    public ItemResponse updateItem(Long id, String ownerEmail, ItemRequest request) {
        RecyclableItem item = findItemById(id);
        assertOwner(item, ownerEmail);
        item.updateDetails(
                request.title().trim(),
                request.description().trim(),
                request.category(),
                request.quantityKg(),
                request.priceTnd(),
                request.location().trim()
        );

        return ItemResponse.from(itemRepository.save(item));
    }

    @Override
    @Transactional
    public void deleteItem(Long id, String ownerEmail) {
        RecyclableItem item = findItemById(id);
        assertOwner(item, ownerEmail);
        itemRepository.delete(item);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private RecyclableItem findItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + id));
    }

    private void assertOwner(RecyclableItem item, String ownerEmail) {
        if (!item.getOwner().getEmail().equals(ownerEmail)) {
            throw new ForbiddenOperationException("Only the item owner can modify this announcement");
        }
    }
}
