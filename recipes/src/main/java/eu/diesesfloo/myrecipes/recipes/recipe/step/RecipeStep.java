package eu.diesesfloo.myrecipes.recipes.recipe.step;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class RecipeStep {

    private final StepType type;

    private final String instruction;

    private final int timer;

    public RecipeStep(String instruction) {
        this(StepType.DEFAULT, instruction, 0);
    }

    public RecipeStep(String instruction, int timer) {
        this(StepType.TIMER, instruction, timer);
    }
}
