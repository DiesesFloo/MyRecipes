package eu.diesesfloo.myrecipes.users.favorite;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service class for managing user favorites.
 */
@Service
@RequiredArgsConstructor
public class UserFavoriteService {

    private final UserFavoriteRepository repository;

    public UserFavorite addFavorite(UUID userId, UUID recipeId, String recipeTitle) {
        UserFavorite favorite = UserFavorite.builder()
                .key(UserFavoriteKey.builder()
                        .userId(userId)
                        .recipeId(recipeId)
                        .build())
                .recipeTitle(recipeTitle != null ? recipeTitle : "")
                .addedAt(Instant.now())
                .build();

        return repository.save(favorite);
    }

    public void removeFavorite(UUID userId, UUID recipeId) {
        repository.deleteByKeyUserIdAndKeyRecipeId(userId, recipeId);
    }

    public List<UserFavorite> getFavorites(UUID userId) {
        return repository.findByKeyUserId(userId);
    }

}
