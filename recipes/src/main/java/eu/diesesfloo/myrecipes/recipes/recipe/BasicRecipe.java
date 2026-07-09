package eu.diesesfloo.myrecipes.recipes.recipe;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class BasicRecipe {

    protected int difficulty;

    protected int cookingTimeMinutes;

    protected int calories;

    protected int proteins;

    protected String title;

    protected String imageUrl;

}
