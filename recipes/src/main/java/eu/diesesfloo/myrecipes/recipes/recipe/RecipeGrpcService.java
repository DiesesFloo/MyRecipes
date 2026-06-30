package eu.diesesfloo.myrecipes.recipes.recipe;

import eu.diesesfloo.myrecipes.recipes.proto.*;
import eu.diesesfloo.myrecipes.recipes.recipe.step.StepType;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.grpc.server.service.GrpcService;

import java.util.List;

/**
 * gRPC service implementation for managing recipes.
 * This service provides methods to add and retrieve recipes.
 */
@GrpcService
@RequiredArgsConstructor
public class RecipeGrpcService extends RecipeServiceGrpc.RecipeServiceImplBase {

    private final RecipeService recipeService;

    @Override
    public void addRecipe(CreateRecipeRequest request, StreamObserver<ChangeResponse> responseObserver) {

    }

    /**
     * Get a recipe by its ID.
     *
     * @param request          Request containing the ID of the recipe to retrieve.
     * @param responseObserver StreamObserver to send the response back to the client.
     */
    @Override
    public void getRecipeById(GetRecipeByIdRequest request, StreamObserver<GetRecipeByIdResponse> responseObserver) {
        responseObserver.onNext(getRecipeById(request));
    }

    /**
     * Retrieve a recipe by its ID and convert it to a GetRecipeByIdResponse.
     *
     * @param request Request containing the ID of the recipe to retrieve.
     * @return GetRecipeByIdResponse containing the recipe details or an error code if not found.
     */
    public GetRecipeByIdResponse getRecipeById(GetRecipeByIdRequest request) {
        return recipeService.getRecipeById(request.getId())
                .map(this::convertToGetByIdResponse)
                .orElseGet(() -> GetRecipeByIdResponse.newBuilder()
                        .setError(404)
                        .build());
    }

    /**
     * Convert a Recipe object to a GetRecipeByIdResponse.
     *
     * @param recipe The Recipe object to convert.
     * @return GetRecipeByIdResponse containing the recipe details.
     */
    private GetRecipeByIdResponse convertToGetByIdResponse(Recipe recipe) {
        return GetRecipeByIdResponse.newBuilder()
                .setId(recipe.getId().toString())
                .setCategory(recipe.getCategory())
                .addAllIngredients(recipe.getIngredients())
                .setDifficulty(recipe.getDifficulty())
                .setServings(recipe.getServings())
                .setCookingTimeMinutes(recipe.getCookingTimeMinutes())
                .setCalories(recipe.getCalories())
                .setProteins(recipe.getProteins())
                .setTitle(recipe.getTitle())
                .setDescription(recipe.getDescription())
                .setImageUrl(recipe.getImageUrl())
                .addAllSteps(getRecipeSteps(recipe))
                .addAllTags(recipe.getTags())
                .build();
    }

    /**
     * Convert the steps of a Recipe object to a list of RecipeStep objects.
     *
     * @param recipe The Recipe object containing the steps to convert.
     * @return List of RecipeStep objects representing the steps of the recipe.
     */
    private List<RecipeStep> getRecipeSteps(Recipe recipe) {
        return recipe.getSteps().stream()
                .map(step -> {
                    RecipeStep.Builder stepBuilder = RecipeStep.newBuilder()
                            .setInstruction(step.getInstruction());

                    if (step.getType() == StepType.TIMER) {
                        stepBuilder.setTimer(step.getTimer());
                        stepBuilder.setType(RecipeStepType.TIMER);
                        return stepBuilder.build();
                    }

                    stepBuilder.setType(RecipeStepType.DEFAULT);
                    return stepBuilder.build();
                })
                .toList();
    }
}
