package eu.diesesfloo.myrecipes.recipes.recipe;

import eu.diesesfloo.myrecipes.recipes.proto.*;
import eu.diesesfloo.myrecipes.recipes.recipe.category.RecipeByCategory;
import eu.diesesfloo.myrecipes.recipes.recipe.category.RecipeByCategoryKey;
import eu.diesesfloo.myrecipes.recipes.recipe.step.Step;
import eu.diesesfloo.myrecipes.recipes.recipe.step.StepType;
import eu.diesesfloo.myrecipes.recipes.recipe.tag.RecipeByTag;
import eu.diesesfloo.myrecipes.recipes.recipe.tag.RecipeByTagKey;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper class for converting between Recipe objects and gRPC request/response objects.
 */
@Component
public class RecipeMapper {

    /**
     * Convert a CreateRecipeRequest to a Recipe object.
     *
     * @param request The create request containing the recipe data.
     * @return A Recipe object constructed from the request data.
     */
    public Recipe recipeFromCreateRequest(CreateRecipeRequest request) {
        return Recipe.builder()
                .id(UUID.randomUUID())
                .category(request.getCategory())
                .ingredients(new HashSet<>(request.getIngredientsList()))
                .difficulty(request.getDifficulty())
                .servings(request.getServings())
                .cookingTimeMinutes(request.getCookingTimeMinutes())
                .calories(request.getCalories())
                .proteins(request.getProteins())
                .title(request.getTitle())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .steps(getStepsFromRequest(request.getStepsList()))
                .tags(new HashSet<>(request.getTagsList()))
                .build();
    }

    /**
     * Convert a Recipe object to a GetRecipeByIdResponse.
     *
     * @param recipe The Recipe object to convert.
     * @return A GetRecipeByIdResponse constructed from the Recipe data.
     */
    public GetRecipeByIdResponse toGetResponse(Recipe recipe) {
        GetRecipeByIdResponse.Builder builder = GetRecipeByIdResponse.newBuilder()
                .setId(recipe.getId() != null ? recipe.getId().toString() : "")
                .setDifficulty(recipe.getDifficulty())
                .setServings(recipe.getServings())
                .setCookingTimeMinutes(recipe.getCookingTimeMinutes())
                .setCalories(recipe.getCalories())
                .setProteins(recipe.getProteins());

        if (recipe.getCategory() != null) {
            builder.setCategory(recipe.getCategory());
        }
        if (recipe.getTitle() != null) {
            builder.setTitle(recipe.getTitle());
        }
        if (recipe.getDescription() != null) {
            builder.setDescription(recipe.getDescription());
        }
        if (recipe.getImageUrl() != null) {
            builder.setImageUrl(recipe.getImageUrl());
        }
        if (recipe.getIngredients() != null) {
            builder.addAllIngredients(recipe.getIngredients());
        }
        if (recipe.getSteps() != null) {
            builder.addAllSteps(getResponseRecipeSteps(recipe));
        }
        if (recipe.getTags() != null) {
            builder.addAllTags(recipe.getTags());
        }

        return builder.build();
    }

    /**
     * Convert a set of {@link RecipeByCategory} objects to a {@link GetRecipesByCategoryResponse}.
     *
     * @param recipes The {@link RecipeByCategory} objects to convert.
     * @return A {@link GetRecipesByCategoryResponse} constructed from the data.
     */
    public GetRecipesByCategoryResponse toGetResponse(Set<RecipeByCategory> recipes) {
        return GetRecipesByCategoryResponse.newBuilder()
                .addAllRecipes(recipes.stream()
                        .map(this::toShortRecipe)
                        .toList())
                .build();
    }

    /**
     * Convert a list of {@link RecipeByCategory} objects to a {@link GetRecipesByCategoryResponse}.
     * Used for paginated results.
     *
     * @param recipes The list of {@link RecipeByCategory} objects to convert.
     * @return A {@link GetRecipesByCategoryResponse} constructed from the data.
     */
    public GetRecipesByCategoryResponse toGetResponse(List<RecipeByCategory> recipes) {
        return GetRecipesByCategoryResponse.newBuilder()
                .addAllRecipes(recipes.stream()
                        .map(this::toShortRecipe)
                        .toList())
                .build();
    }

    /**
     * Convert a set of {@link RecipeByTag} objects to a {@link GetRecipesByTagResponse}.
     *
     * @param recipes The {@link RecipeByTag} objects to convert.
     * @return A {@link GetRecipesByTagResponse} constructed from the data.
     */
    public GetRecipesByTagResponse toTagGetResponse(Set<RecipeByTag> recipes) {
        return GetRecipesByTagResponse.newBuilder()
                .addAllRecipes(recipes.stream()
                        .map(this::toShortRecipe)
                        .toList())
                .build();
    }

    /**
     * Convert a {@link RecipeByCategory} object to a {@link ShortRecipe}.
     *
     * @param recipe The recipe to convert
     * @return A {@link ShortRecipe} constructed from the data.
     */
    private ShortRecipe toShortRecipe(RecipeByCategory recipe) {
        ShortRecipe.Builder builder = ShortRecipe.newBuilder()
                .setCalories(recipe.getCalories())
                .setDifficulty(recipe.getDifficulty())
                .setProteins(recipe.getProteins())
                .setCookingTimeMinutes(recipe.getCookingTimeMinutes());

        if (recipe.getKey() != null) {
            if (recipe.getKey().getCategory() != null) {
                builder.setCategory(recipe.getKey().getCategory());
            }
            if (recipe.getKey().getId() != null) {
                builder.setId(recipe.getKey().getId().toString());
            }
        }
        if (recipe.getTitle() != null) {
            builder.setTitle(recipe.getTitle());
        }
        if (recipe.getImageUrl() != null) {
            builder.setImageUrl(recipe.getImageUrl());
        }

        return builder.build();
    }

    /**
     * Convert a {@link RecipeByTag} object to a {@link ShortRecipe}.
     *
     * @param recipe The recipe to convert
     * @return A {@link ShortRecipe} constructed from the data.
     */
    private ShortRecipe toShortRecipe(RecipeByTag recipe) {
        ShortRecipe.Builder builder = ShortRecipe.newBuilder()
                .setCalories(recipe.getCalories())
                .setDifficulty(recipe.getDifficulty())
                .setProteins(recipe.getProteins())
                .setCookingTimeMinutes(recipe.getCookingTimeMinutes());

        if (recipe.getCategory() != null) {
            builder.setCategory(recipe.getCategory());
        }
        if (recipe.getKey() != null && recipe.getKey().getId() != null) {
            builder.setId(recipe.getKey().getId().toString());
        }
        if (recipe.getTitle() != null) {
            builder.setTitle(recipe.getTitle());
        }
        if (recipe.getImageUrl() != null) {
            builder.setImageUrl(recipe.getImageUrl());
        }

        return builder.build();
    }

    /**
     * Convert the steps of a Recipe object to a list of RecipeStep objects.
     *
     * @param recipe The Recipe object containing the steps to convert.
     * @return List of RecipeStep objects representing the steps of the recipe.
     */
    private List<RecipeStep> getResponseRecipeSteps(Recipe recipe) {
        if (recipe.getSteps() == null) {
            return List.of();
        }
        return recipe.getSteps().stream()
                .map(step -> {
                    RecipeStep.Builder stepBuilder = RecipeStep.newBuilder();

                    if (step.getInstruction() != null) {
                        stepBuilder.setInstruction(step.getInstruction());
                    }

                    if (step.getType() == StepType.TIMER) {
                        stepBuilder.setTimer(step.getTimer());
                        stepBuilder.setType(RecipeStepType.TIMER);
                    } else {
                        stepBuilder.setType(RecipeStepType.DEFAULT);
                    }

                    return stepBuilder.build();
                })
                .toList();
    }

    /**
     * Convert a list of RecipeStep objects from a request to a list of Step objects.
     *
     * @param steps List of RecipeStep objects from the request.
     * @return List of Step objects constructed from the request data.
     */
    private List<Step> getStepsFromRequest(List<RecipeStep> steps) {
        return steps.stream()
                .map(step -> {
                    if (step.getType() == RecipeStepType.TIMER) {
                        return new Step(step.getInstruction(), step.getTimer());
                    }
                    return new Step(step.getInstruction());
                })
                .toList();
    }

    /**
     * Apply updates from an UpdateRecipeRequest to an existing Recipe object.
     *
     * @param req    The update request containing the new recipe data.
     * @param recipe The existing Recipe object to be updated.
     */
    public void applyUpdates(UpdateRecipeRequest req, Recipe recipe) {
        applyMain(req, recipe);
        applyBasics(req, recipe);
        applyIngredients(req, recipe);
        applySteps(req, recipe);
        applyTags(req, recipe);
    }

    public void applyUpdates(UpdateRecipeRequest req, RecipeByCategory recipe) {
        applyBasics(req, recipe);
        if (req.hasCategory()) recipe.getKey().setCategory(req.getCategory());
    }

    /**
     * Apply basic updates (category, difficulty, servings, cooking time, title, description, image URL) from the request to the recipe.
     *
     * @param req    The update request containing the new recipe data.
     * @param recipe The existing Recipe object to be updated.
     */
    private void applyMain(UpdateRecipeRequest req, Recipe recipe) {
        if (req.hasCategory()) recipe.setCategory(req.getCategory());
        if (req.hasServings()) recipe.setServings(req.getServings());
        if (req.hasDescription()) recipe.setDescription(req.getDescription());
    }

    private void applyBasics(UpdateRecipeRequest req, BasicRecipe recipe) {
        if (req.hasDifficulty()) recipe.setDifficulty(req.getDifficulty());
        if (req.hasCookingTimeMinutes()) recipe.setCookingTimeMinutes(req.getCookingTimeMinutes());
        if (req.hasTitle()) recipe.setTitle(req.getTitle());
        if (req.hasImageUrl()) recipe.setImageUrl(req.getImageUrl());
        if (req.hasCalories()) recipe.setCalories(req.getCalories());
        if (req.hasProteins()) recipe.setProteins(req.getProteins());
    }

    /**
     * Apply ingredient updates from the request to the recipe.
     *
     * @param req    The update request containing the new recipe data.
     * @param recipe The existing Recipe object to be updated.
     */
    private void applyIngredients(UpdateRecipeRequest req, Recipe recipe) {
        if (req.getIngredientsCount() > 0) {
            recipe.setIngredients(new HashSet<>(req.getIngredientsList()));
        }
    }

    /**
     * Apply step updates from the request to the recipe.
     *
     * @param req    The update request containing the new recipe data.
     * @param recipe The existing Recipe object to be updated.
     */
    private void applySteps(UpdateRecipeRequest req, Recipe recipe) {
        if (req.getStepsCount() > 0) {
            recipe.setSteps(getStepsFromRequest(req.getStepsList()));
        }
    }

    /**
     * Apply tag updates from the request to the recipe.
     *
     * @param req    The update request containing the new recipe data.
     * @param recipe The existing Recipe object to be updated.
     */
    private void applyTags(UpdateRecipeRequest req, Recipe recipe) {
        if (req.getTagsCount() > 0) {
            recipe.setTags(new HashSet<>(req.getTagsList()));
        }
    }

    /**
     * Convert a {@link Recipe} object to a {@link RecipeByCategory} object.
     *
     * @param recipe The {@link Recipe} object containing the steps to convert.
     * @return A {@link RecipeByCategory} object constructed from the {@link Recipe} data.
     */
    public RecipeByCategory toByCategory(Recipe recipe) {
        return RecipeByCategory.builder()
                .key(RecipeByCategoryKey.builder()
                        .category(recipe.getCategory())
                        .id(recipe.getId())
                        .build())
                .title(recipe.getTitle())
                .difficulty(recipe.getDifficulty())
                .cookingTimeMinutes(recipe.getCookingTimeMinutes())
                .calories(recipe.getCalories())
                .proteins(recipe.getProteins())
                .imageUrl(recipe.getImageUrl())
                .build();
    }

    /**
     * Convert a {@link Recipe} to a set of {@link RecipeByTag} entries — one per tag.
     *
     * @param recipe The recipe to convert.
     * @return A set of {@link RecipeByTag} entries, one for each tag on the recipe.
     */
    public Set<RecipeByTag> toByTags(Recipe recipe) {
        if (recipe.getTags() == null || recipe.getTags().isEmpty()) {
            return Set.of();
        }

        return recipe.getTags().stream()
                .map(tag -> RecipeByTag.builder()
                        .key(RecipeByTagKey.builder()
                                .tag(tag)
                                .id(recipe.getId())
                                .build())
                        .category(recipe.getCategory())
                        .title(recipe.getTitle())
                        .difficulty(recipe.getDifficulty())
                        .cookingTimeMinutes(recipe.getCookingTimeMinutes())
                        .calories(recipe.getCalories())
                        .proteins(recipe.getProteins())
                        .imageUrl(recipe.getImageUrl())
                        .build())
                .collect(Collectors.toSet());
    }

}
