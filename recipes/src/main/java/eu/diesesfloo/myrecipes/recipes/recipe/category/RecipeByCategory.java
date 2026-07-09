package eu.diesesfloo.myrecipes.recipes.recipe.category;

import eu.diesesfloo.myrecipes.recipes.recipe.BasicRecipe;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@EqualsAndHashCode(callSuper = true)
@Table("recipes_by_category")
@Data
@SuperBuilder
public class RecipeByCategory extends BasicRecipe {

    @PrimaryKey
    private RecipeByCategoryKey key;

}
