package eu.diesesfloo.myrecipes.users.rating;

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
class RecipeRatingServiceTest {

    @Mock
    private RecipeRatingRepository repository;

    @InjectMocks
    private RecipeRatingService service;

    @Test
    @DisplayName("Should save recipe rating")
    void testRateRecipe() {
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        RecipeRating rating = RecipeRating.builder()
                .key(RecipeRatingKey.builder().userId(userId).recipeId(recipeId).build())
                .rating(5)
                .comment("Perfection!")
                .ratedAt(Instant.now())
                .build();

        when(repository.save(any(RecipeRating.class))).thenReturn(rating);

        RecipeRating result = service.rateRecipe(userId, recipeId, 5, "Perfection!");

        assertNotNull(result);
        assertEquals(5, result.getRating());
        assertEquals("Perfection!", result.getComment());
        verify(repository).save(any(RecipeRating.class));
    }

    @Test
    @DisplayName("Should retrieve ratings for a user")
    void testGetUserRatings() {
        UUID userId = UUID.randomUUID();
        RecipeRating r1 = RecipeRating.builder().rating(4).comment("Good").build();
        RecipeRating r2 = RecipeRating.builder().rating(5).comment("Great").build();

        when(repository.findByKeyUserId(userId)).thenReturn(List.of(r1, r2));

        List<RecipeRating> ratings = service.getUserRatings(userId);

        assertEquals(2, ratings.size());
        assertEquals(4, ratings.get(0).getRating());
        assertEquals(5, ratings.get(1).getRating());
    }
}
