package eu.diesesfloo.myrecipes.recipes.recipe.category;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.util.UUID;

import static org.springframework.data.cassandra.core.cql.PrimaryKeyType.CLUSTERED;
import static org.springframework.data.cassandra.core.cql.PrimaryKeyType.PARTITIONED;

/**
 * Composite primary key for the {@code recipes_by_category} table.
 * Partitions by category and clusters by recipe ID.
 */
@Data
@Builder
@PrimaryKeyClass
public class RecipeByCategoryKey {

    @PrimaryKeyColumn(type = PARTITIONED)
    private String category;

    @PrimaryKeyColumn(type = CLUSTERED)
    private UUID id;

}
