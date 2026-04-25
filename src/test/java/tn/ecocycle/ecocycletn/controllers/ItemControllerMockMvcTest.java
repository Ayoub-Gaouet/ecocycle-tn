package tn.ecocycle.ecocycletn.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tn.ecocycle.ecocycletn.dto.ItemRequest;
import tn.ecocycle.ecocycletn.dto.ItemResponse;
import tn.ecocycle.ecocycletn.entities.Category;
import tn.ecocycle.ecocycletn.entities.Role;
import tn.ecocycle.ecocycletn.entities.User;
import tn.ecocycle.ecocycletn.exceptions.ForbiddenOperationException;
import tn.ecocycle.ecocycletn.exceptions.GlobalExceptionHandler;
import tn.ecocycle.ecocycletn.repositories.UserRepository;
import tn.ecocycle.ecocycletn.security.JwtAuthFilter;
import tn.ecocycle.ecocycletn.security.JwtProperties;
import tn.ecocycle.ecocycletn.security.JwtService;
import tn.ecocycle.ecocycletn.security.SecurityConfig;
import tn.ecocycle.ecocycletn.services.ItemService;

@WebMvcTest(ItemController.class)
@Import({
        GlobalExceptionHandler.class,
        SecurityConfig.class,
        JwtAuthFilter.class,
        JwtService.class
})
@EnableConfigurationProperties(JwtProperties.class)
@TestPropertySource(properties = {
        "app.security.jwt.secret=item-test-secret-with-at-least-32-bytes",
        "app.security.jwt.expiration=PT1H"
})
class ItemControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void createItemReturnsCreatedWithValidJwt() throws Exception {
        ItemRequest request = itemRequest("Plastic bottles");
        when(itemService.createItem(eq("owner@example.com"), any(ItemRequest.class)))
                .thenReturn(itemResponse(1L, "Plastic bottles", Category.PLASTIC));

        mockMvc.perform(post("/api/items")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("owner@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/items/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Plastic bottles"))
                .andExpect(jsonPath("$.category").value("PLASTIC"))
                .andExpect(jsonPath("$.ownerEmail").value("owner@example.com"));
    }

    @Test
    void listItemsSupportsCategoryFilter() throws Exception {
        when(itemService.listItems(Category.PLASTIC))
                .thenReturn(List.of(itemResponse(1L, "Plastic bottles", Category.PLASTIC)));

        mockMvc.perform(get("/api/items")
                        .param("category", "PLASTIC")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("collector@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].category").value("PLASTIC"));
    }

    @Test
    void getItemReturnsAnnouncementDetails() throws Exception {
        when(itemService.getItem(1L))
                .thenReturn(itemResponse(1L, "Plastic bottles", Category.PLASTIC));

        mockMvc.perform(get("/api/items/1")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("collector@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Plastic bottles"));
    }

    @Test
    void updateItemReturnsUpdatedAnnouncement() throws Exception {
        ItemRequest request = itemRequest("Paper bags");
        when(itemService.updateItem(eq(1L), eq("owner@example.com"), any(ItemRequest.class)))
                .thenReturn(itemResponse(1L, "Paper bags", Category.PAPER));

        mockMvc.perform(put("/api/items/1")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("owner@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Paper bags"))
                .andExpect(jsonPath("$.category").value("PAPER"));
    }

    @Test
    void deleteItemReturnsNoContent() throws Exception {
        doNothing().when(itemService).deleteItem(1L, "owner@example.com");

        mockMvc.perform(delete("/api/items/1")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("owner@example.com")))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateItemRejectsNonOwnerWithForbidden() throws Exception {
        when(itemService.updateItem(eq(1L), eq("other@example.com"), any(ItemRequest.class)))
                .thenThrow(new ForbiddenOperationException("Only the item owner can modify this announcement"));

        mockMvc.perform(put("/api/items/1")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("other@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequest("Paper bags"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Only the item owner can modify this announcement"));
    }

    @Test
    void createItemRejectsMissingJwt() throws Exception {
        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequest("Plastic bottles"))))
                .andExpect(status().isUnauthorized());
    }

    private ItemRequest itemRequest(String title) {
        Category category = title.contains("Paper") ? Category.PAPER : Category.PLASTIC;
        return new ItemRequest(
                title,
                "Reusable waste",
                category,
                new BigDecimal("2.50"),
                new BigDecimal("1.25"),
                "Tunis"
        );
    }

    private ItemResponse itemResponse(Long id, String title, Category category) {
        return new ItemResponse(
                id,
                title,
                "Reusable waste",
                category,
                new BigDecimal("2.50"),
                new BigDecimal("1.25"),
                "Tunis",
                "owner@example.com",
                "Owner User"
        );
    }

    private String bearerToken(String email) {
        User user = new User(email, "hashed-password", "Test User", Role.USER);
        return "Bearer " + jwtService.generateToken(user);
    }
}
