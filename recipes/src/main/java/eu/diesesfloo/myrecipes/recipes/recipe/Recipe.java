package eu.diesesfloo.myrecipes.recipes.recipe;

import eu.diesesfloo.myrecipes.recipes.recipe.step.Step;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Table("recipes_by_id")
@Data
@Builder
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

    private List<Step> steps;

    private Set<String> tags;

}
