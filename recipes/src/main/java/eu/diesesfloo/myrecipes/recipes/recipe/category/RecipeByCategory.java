package eu.diesesfloo.myrecipes.recipes.recipe.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("recipes_by_category")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeByCategory {

    @PrimaryKey
    private RecipeByCategoryKey key;

    private int difficulty;

    private int cookingTimeMinutes;

    private int calories;

    private int proteins;

    private String title;

    private String imageUrl;

}
