package eu.diesesfloo.myrecipes.recipes.recipe;

import eu.diesesfloo.myrecipes.recipes.recipe.step.Step;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Table("recipes_by_id")
@Data
@SuperBuilder
public class Recipe extends BasicRecipe {

    @PrimaryKey
    private UUID id;

    private String category;

    private Set<String> ingredients;

    private int servings;

    private String description;

    private List<Step> steps;

    private Set<String> tags;

}
