package eu.diesesfloo.myrecipes.recipes.recipe.tag;

import eu.diesesfloo.myrecipes.recipes.recipe.BasicRecipe;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * Denormalized view of recipes indexed by tag.
 * Follows the same pattern as {@code RecipeByCategory} — stores just the fields
 * needed for listing/card display to avoid fetching the full recipe.
 */
@EqualsAndHashCode(callSuper = true)
@Table("recipes_by_tag")
@Data
@SuperBuilder
public class RecipeByTag extends BasicRecipe {

    @PrimaryKey
    private RecipeByTagKey key;

    private String category;

}
