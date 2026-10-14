package eu.diesesfloo.myrecipes.recipes.recipe.tag;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.util.UUID;

import static org.springframework.data.cassandra.core.cql.PrimaryKeyType.CLUSTERED;
import static org.springframework.data.cassandra.core.cql.PrimaryKeyType.PARTITIONED;

/**
 * Composite primary key for the {@code recipes_by_tag} table.
 * Partitions by tag and clusters by recipe ID for efficient lookups.
 */
@Data
@Builder
@PrimaryKeyClass
public class RecipeByTagKey {

    @PrimaryKeyColumn(type = PARTITIONED)
    private String tag;

    @PrimaryKeyColumn(type = CLUSTERED)
    private UUID id;

}
