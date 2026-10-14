package eu.diesesfloo.myrecipes.users.rating;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.util.UUID;

import static org.springframework.data.cassandra.core.cql.PrimaryKeyType.CLUSTERED;
import static org.springframework.data.cassandra.core.cql.PrimaryKeyType.PARTITIONED;

/**
 * Composite key for recipe ratings partitioned by user and clustered by recipe ID.
 */
@Data
@Builder
@PrimaryKeyClass
public class RecipeRatingKey {

    @PrimaryKeyColumn(type = PARTITIONED)
    private UUID userId;

    @PrimaryKeyColumn(type = CLUSTERED)
    private UUID recipeId;

}
