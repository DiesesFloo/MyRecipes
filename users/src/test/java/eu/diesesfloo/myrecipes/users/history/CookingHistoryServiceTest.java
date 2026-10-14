package eu.diesesfloo.myrecipes.users.history;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CookingHistoryServiceTest {

    @Mock
    private CookingHistoryRepository repository;

    @InjectMocks
    private CookingHistoryService service;

    @Test
    @DisplayName("Should record cooking history entry")
    void testRecordCooking() {
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        CookingHistory history = CookingHistory.builder()
                .key(CookingHistoryKey.builder().userId(userId).recipeId(recipeId).cookedAt(Instant.now()).build())
                .recipeTitle("Ramen")
                .notes("Added extra egg")
                .build();

        when(repository.save(any(CookingHistory.class))).thenReturn(history);

        CookingHistory result = service.recordCooking(userId, recipeId, "Ramen", "Added extra egg");

        assertNotNull(result);
        assertEquals("Ramen", result.getRecipeTitle());
        assertEquals("Added extra egg", result.getNotes());
        verify(repository).save(any(CookingHistory.class));
    }

    @Test
    @DisplayName("Should get history with limit applied")
    void testGetHistory_WithLimit() {
        UUID userId = UUID.randomUUID();
        CookingHistory h1 = CookingHistory.builder().recipeTitle("Dish 1").build();
        CookingHistory h2 = CookingHistory.builder().recipeTitle("Dish 2").build();
        CookingHistory h3 = CookingHistory.builder().recipeTitle("Dish 3").build();

        when(repository.findByKeyUserId(userId)).thenReturn(List.of(h1, h2, h3));

        List<CookingHistory> result = service.getHistory(userId, 2);

        assertEquals(2, result.size());
        assertEquals("Dish 1", result.get(0).getRecipeTitle());
        assertEquals("Dish 2", result.get(1).getRecipeTitle());
    }

    @Test
    @DisplayName("Should get entire history when limit is 0 or negative")
    void testGetHistory_NoLimit() {
        UUID userId = UUID.randomUUID();
        CookingHistory h1 = CookingHistory.builder().recipeTitle("Dish 1").build();
        CookingHistory h2 = CookingHistory.builder().recipeTitle("Dish 2").build();

        when(repository.findByKeyUserId(userId)).thenReturn(List.of(h1, h2));

        List<CookingHistory> result = service.getHistory(userId, 0);

        assertEquals(2, result.size());
    }
}
