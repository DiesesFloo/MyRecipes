package eu.diesesfloo.myrecipes.users.rating;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service class for managing recipe ratings.
 */
@Service
@RequiredArgsConstructor
public class RecipeRatingService {

    private final RecipeRatingRepository repository;

    public RecipeRating rateRecipe(UUID userId, UUID recipeId, int rating, String comment) {
        RecipeRating recipeRating = RecipeRating.builder()
                .key(RecipeRatingKey.builder()
                        .userId(userId)
                        .recipeId(recipeId)
                        .build())
                .rating(rating)
                .comment(comment != null ? comment : "")
                .ratedAt(Instant.now())
                .build();

        return repository.save(recipeRating);
    }

    public List<RecipeRating> getUserRatings(UUID userId) {
        return repository.findByKeyUserId(userId);
    }

}
