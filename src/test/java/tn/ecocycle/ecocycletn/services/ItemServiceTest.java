package tn.ecocycle.ecocycletn.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tn.ecocycle.ecocycletn.dto.ItemRequest;
import tn.ecocycle.ecocycletn.dto.ItemResponse;
import tn.ecocycle.ecocycletn.entities.Category;
import tn.ecocycle.ecocycletn.entities.RecyclableItem;
import tn.ecocycle.ecocycletn.entities.Role;
import tn.ecocycle.ecocycletn.entities.User;
import tn.ecocycle.ecocycletn.exceptions.ForbiddenOperationException;
import tn.ecocycle.ecocycletn.exceptions.ResourceNotFoundException;
import tn.ecocycle.ecocycletn.repositories.ItemRepository;
import tn.ecocycle.ecocycletn.repositories.UserRepository;
import tn.ecocycle.ecocycletn.services.impl.ItemServiceImpl;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    private ItemService itemService;

    @BeforeEach
    void setUp() {
        itemService = new ItemServiceImpl(itemRepository, userRepository);
    }

    @Test
    void createItemCreatesAnnouncementForAuthenticatedUser() {
        User owner = user("owner@example.com", "Owner User");
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(RecyclableItem.class))).thenAnswer(invocation -> {
            RecyclableItem saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 10L);
            return saved;
        });

        ItemResponse response = itemService.createItem("owner@example.com", itemRequest("Plastic bottles"));

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.title()).isEqualTo("Plastic bottles");
        assertThat(response.category()).isEqualTo(Category.PLASTIC);
        assertThat(response.ownerEmail()).isEqualTo("owner@example.com");
    }

    @Test
    void listItemsFiltersByCategoryWhenProvided() {
        RecyclableItem item = item(1L, user("owner@example.com", "Owner User"));
        when(itemRepository.findByCategory(Category.PLASTIC)).thenReturn(List.of(item));

        List<ItemResponse> response = itemService.listItems(Category.PLASTIC);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).category()).isEqualTo(Category.PLASTIC);
        verify(itemRepository).findByCategory(Category.PLASTIC);
        verify(itemRepository, never()).findAll();
    }

    @Test
    void getItemReturnsAnnouncementDetails() {
        RecyclableItem item = item(1L, user("owner@example.com", "Owner User"));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ItemResponse response = itemService.getItem(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.ownerFullName()).isEqualTo("Owner User");
    }

    @Test
    void updateItemAllowsOwnerOnly() {
        RecyclableItem item = item(1L, user("owner@example.com", "Owner User"));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(RecyclableItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemResponse response = itemService.updateItem(
                1L,
                "owner@example.com",
                new ItemRequest(
                        " Updated paper ",
                        " Clean paper bags ",
                        Category.PAPER,
                        new BigDecimal("3.50"),
                        new BigDecimal("2.00"),
                        " Sfax "
                )
        );

        assertThat(response.title()).isEqualTo("Updated paper");
        assertThat(response.description()).isEqualTo("Clean paper bags");
        assertThat(response.category()).isEqualTo(Category.PAPER);
        assertThat(response.location()).isEqualTo("Sfax");
        verify(itemRepository).save(item);
    }

    @Test
    void updateItemRejectsNonOwner() {
        RecyclableItem item = item(1L, user("owner@example.com", "Owner User"));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> itemService.updateItem(1L, "other@example.com", itemRequest("Updated title")))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Only the item owner can modify this announcement");

        verify(itemRepository, never()).save(any(RecyclableItem.class));
    }

    @Test
    void deleteItemAllowsOwnerOnly() {
        RecyclableItem item = item(1L, user("owner@example.com", "Owner User"));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        itemService.deleteItem(1L, "owner@example.com");

        verify(itemRepository).delete(item);
    }

    @Test
    void getItemFailsWhenAnnouncementDoesNotExist() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getItem(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Item not found: 99");
    }

    private ItemRequest itemRequest(String title) {
        return new ItemRequest(
                title,
                "Reusable plastic waste",
                Category.PLASTIC,
                new BigDecimal("2.50"),
                new BigDecimal("1.25"),
                "Tunis"
        );
    }

    private RecyclableItem item(Long id, User owner) {
        RecyclableItem item = new RecyclableItem(
                "Plastic bottles",
                "Reusable plastic waste",
                Category.PLASTIC,
                new BigDecimal("2.50"),
                new BigDecimal("1.25"),
                "Tunis",
                owner
        );
        ReflectionTestUtils.setField(item, "id", id);
        return item;
    }

    private User user(String email, String fullName) {
        return new User(email, "hashed-password", fullName, Role.USER);
    }
}
