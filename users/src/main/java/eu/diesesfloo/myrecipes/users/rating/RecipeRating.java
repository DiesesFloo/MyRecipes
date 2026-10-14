package eu.diesesfloo.myrecipes.users.rating;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;

/**
 * Cassandra entity representing a user's rating for a recipe (1-5 stars).
 */
@Table("ratings_by_user")
@Data
@Builder
public class RecipeRating {

    @PrimaryKey
    private RecipeRatingKey key;

    private int rating;

    private String comment;

    private Instant ratedAt;

}
