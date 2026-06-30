package eu.diesesfloo.myrecipes.recipes.recipe.step;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class Step {

    private final StepType type;

    private final String instruction;

    private final int timer;

    public Step(String instruction) {
        this(StepType.DEFAULT, instruction, 0);
    }

    public Step(String instruction, int timer) {
        this(StepType.TIMER, instruction, timer);
    }
}
