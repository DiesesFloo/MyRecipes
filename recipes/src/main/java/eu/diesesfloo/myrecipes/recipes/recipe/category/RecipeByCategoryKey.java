package eu.diesesfloo.myrecipes.recipes.recipe.category;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;

import java.util.UUID;

@Data
@Builder
@PrimaryKeyClass
public class RecipeByCategoryKey {

    private String category;

    private UUID id;

}
