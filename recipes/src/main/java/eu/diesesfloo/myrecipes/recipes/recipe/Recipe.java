package eu.diesesfloo.myrecipes.recipes.recipe;

import eu.diesesfloo.myrecipes.recipes.recipe.step.RecipeStep;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Table("recipes_by_id")
@Data
@Accessors(chain=true)
public class Recipe {

    @PrimaryKey
    private UUID id;

    private String category;

    private Set<String> ingredients;

    private int difficulty;

    private int servings;

    private int cookingTimeMinutes;

    private int calories;

    private int proteins;

    private String title;

    private String description;

    private String imageUrl;

    private List<RecipeStep> steps;

    private Set<String> tags;

}
