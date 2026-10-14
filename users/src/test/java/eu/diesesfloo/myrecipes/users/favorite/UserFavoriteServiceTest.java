package eu.diesesfloo.myrecipes.users.favorite;

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
class UserFavoriteServiceTest {

    @Mock
    private UserFavoriteRepository repository;

    @InjectMocks
    private UserFavoriteService service;

    @Test
    @DisplayName("Should add recipe to user favorites")
    void testAddFavorite() {
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        UserFavorite favorite = UserFavorite.builder()
                .key(UserFavoriteKey.builder().userId(userId).recipeId(recipeId).build())
                .recipeTitle("Lasagna")
                .addedAt(Instant.now())
                .build();

        when(repository.save(any(UserFavorite.class))).thenReturn(favorite);

        UserFavorite result = service.addFavorite(userId, recipeId, "Lasagna");

        assertNotNull(result);
        assertEquals("Lasagna", result.getRecipeTitle());
        verify(repository).save(any(UserFavorite.class));
    }

    @Test
    @DisplayName("Should remove recipe from user favorites")
    void testRemoveFavorite() {
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();

        service.removeFavorite(userId, recipeId);

        verify(repository).deleteByKeyUserIdAndKeyRecipeId(userId, recipeId);
    }

    @Test
    @DisplayName("Should get all user favorites")
    void testGetFavorites() {
        UUID userId = UUID.randomUUID();
        UserFavorite f1 = UserFavorite.builder().recipeTitle("Lasagna").build();
        UserFavorite f2 = UserFavorite.builder().recipeTitle("Risotto").build();

        when(repository.findByKeyUserId(userId)).thenReturn(List.of(f1, f2));

        List<UserFavorite> result = service.getFavorites(userId);

        assertEquals(2, result.size());
        assertEquals("Lasagna", result.get(0).getRecipeTitle());
        assertEquals("Risotto", result.get(1).getRecipeTitle());
    }
}
