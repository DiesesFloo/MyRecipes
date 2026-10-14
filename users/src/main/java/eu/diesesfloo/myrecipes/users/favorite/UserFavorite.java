package eu.diesesfloo.myrecipes.users.favorite;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;

/**
 * Cassandra entity representing a recipe saved to a user's favorites.
 */
@Table("favorites_by_user")
@Data
@Builder
public class UserFavorite {

    @PrimaryKey
    private UserFavoriteKey key;

    private String recipeTitle;

    private Instant addedAt;

}
