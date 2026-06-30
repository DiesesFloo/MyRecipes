package eu.diesesfloo.myrecipes.recipes.recipe;

import eu.diesesfloo.myrecipes.recipes.proto.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.grpc.server.service.GrpcService;

/**
 * gRPC service implementation for managing recipes.
 * This service provides methods to add and retrieve recipes.
 */
@GrpcService
@RequiredArgsConstructor
public class RecipeGrpcService extends RecipeServiceGrpc.RecipeServiceImplBase {

    private final RecipeService recipeService;
    private final RecipeMapper recipeMapper;

    /**
     * Add a new recipe based on the provided request.
     *
     * @param request          The create request containing the recipe data.
     * @param responseObserver StreamObserver to send the response back to the client.
     */
    @Override
    public void addRecipe(CreateRecipeRequest request, StreamObserver<ChangeResponse> responseObserver) {
        recipeService.saveRecipe(recipeMapper.fromCreateRequest(request));

        responseObserver.onNext(ChangeResponse.newBuilder().setSuccess(true).build());
    }

    /**
     * Delete a recipe based on the provided request.
     *
     * @param request          The delete request containing the recipe ID to be deleted.
     * @param responseObserver StreamObserver to send the response back to the client.
     */
    @Override
    public void deleteRecipe(DeleteRecipeRequest request, StreamObserver<ChangeResponse> responseObserver) {
        recipeService.deleteRecipe(request.getUuid());
        responseObserver.onNext(ChangeResponse.newBuilder().setSuccess(true).build());
    }

    /**
     * Update an existing recipe based on the provided request.
     *
     * @param request          The update request containing the new recipe data.
     * @param responseObserver StreamObserver to send the response back to the client.
     */
    @Override
    public void updateRecipe(UpdateRecipeRequest request, StreamObserver<ChangeResponse> responseObserver) {
        recipeService.getRecipeById(request.getId())
                .ifPresentOrElse(
                        recipe -> responseObserver.onNext(handleUpdate(request, recipe)),
                        () -> responseObserver.onNext(ChangeResponse.newBuilder()
                                .setSuccess(false)
                                .setError(404)
                                .build())
                );
    }

    /**
     * Handle the update of a recipe based on the provided request and existing recipe.
     *
     * @param request The update request containing the new recipe data.
     * @param recipe  The existing recipe to be updated.
     * @return ChangeResponse indicating the success or failure of the update operation.
     */
    private ChangeResponse handleUpdate(UpdateRecipeRequest request, Recipe recipe) {
        recipeMapper.applyUpdates(request, recipe);

        recipeService.saveRecipe(recipe);
        return ChangeResponse.newBuilder().setSuccess(true).build();
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
    private GetRecipeByIdResponse getRecipeById(GetRecipeByIdRequest request) {
        return recipeService.getRecipeById(request.getId())
                .map(recipeMapper::toGetResponse)
                .orElseGet(() -> GetRecipeByIdResponse.newBuilder()
                        .setError(404)
                        .build());
    }

}
