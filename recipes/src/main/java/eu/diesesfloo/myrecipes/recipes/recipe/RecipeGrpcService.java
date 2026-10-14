package eu.diesesfloo.myrecipes.recipes.recipe;

import eu.diesesfloo.myrecipes.recipes.event.RecipeEventPublisher;
import eu.diesesfloo.myrecipes.recipes.proto.*;
import eu.diesesfloo.myrecipes.recipes.recipe.category.RecipeByCategory;
import eu.diesesfloo.myrecipes.recipes.recipe.category.RecipeByCategoryService;
import eu.diesesfloo.myrecipes.recipes.recipe.tag.RecipeByTagService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.grpc.server.service.GrpcService;

import java.util.HashSet;

/**
 * gRPC service implementation for managing recipes.
 * This service provides methods to add, update, delete, and retrieve recipes
 * with input validation, Kafka event publishing, and tag-based search.
 */
@GrpcService
@RequiredArgsConstructor
public class RecipeGrpcService extends RecipeServiceGrpc.RecipeServiceImplBase {

    private final RecipeService recipeService;
    private final RecipeByCategoryService recipeByCategoryService;
    private final RecipeByTagService recipeByTagService;
    private final RecipeMapper recipeMapper;
    private final RecipeEventPublisher recipeEventPublisher;

    /**
     * Add a new recipe based on the provided request.
     * Validates input, persists to all denormalized tables, and publishes a Kafka event.
     *
     * @param request          The create request containing the recipe data.
     * @param responseObserver StreamObserver to send the response back to the client.
     */
    @Override
    public void addRecipe(CreateRecipeRequest request, StreamObserver<ChangeResponse> responseObserver) {
        Status validationError = validateCreateRequest(request);
        if (validationError != null) {
            responseObserver.onError(validationError.asRuntimeException());
            return;
        }

        Recipe recipe = recipeMapper.recipeFromCreateRequest(request);
        recipeService.saveRecipe(recipe);
        recipeByCategoryService.saveRecipe(recipeMapper.toByCategory(recipe));
        recipeByTagService.saveAll(recipeMapper.toByTags(recipe));

        recipeEventPublisher.publishCreated(recipe);

        responseObserver.onNext(ChangeResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    /**
     * Delete a recipe based on the provided request.
     * Removes from all denormalized tables and publishes a Kafka event.
     *
     * @param request          The delete request containing the recipe ID to be deleted.
     * @param responseObserver StreamObserver to send the response back to the client.
     */
    @Override
    public void deleteRecipe(DeleteRecipeRequest request, StreamObserver<ChangeResponse> responseObserver) {
        if (request.getUuid().isBlank()) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription("Recipe ID must not be empty").asRuntimeException());
            return;
        }

        recipeService.deleteRecipe(request.getUuid());
        recipeByCategoryService.deleteRecipe(request.getUuid());
        recipeByTagService.deleteByRecipeId(request.getUuid());

        recipeEventPublisher.publishDeleted(request.getUuid());

        responseObserver.onNext(ChangeResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    /**
     * Update an existing recipe based on the provided request.
     * Validates input, applies partial updates, and publishes a Kafka event.
     *
     * @param request          The update request containing the new recipe data.
     * @param responseObserver StreamObserver to send the response back to the client.
     */
    @Override
    public void updateRecipe(UpdateRecipeRequest request, StreamObserver<ChangeResponse> responseObserver) {
        if (request.getId().isBlank()) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription("Recipe ID must not be empty").asRuntimeException());
            return;
        }

        Status validationError = validateUpdateRequest(request);
        if (validationError != null) {
            responseObserver.onError(validationError.asRuntimeException());
            return;
        }

        recipeService.getRecipeById(request.getId())
                .ifPresentOrElse(
                        recipe -> {
                            ChangeResponse result = handleUpdate(
                                    request,
                                    recipe,
                                    recipeByCategoryService.getById(request.getId()));
                            responseObserver.onNext(result);
                            responseObserver.onCompleted();
                        },
                        () -> responseObserver.onError(
                                Status.NOT_FOUND
                                        .withDescription("Recipe not found: " + request.getId())
                                        .asRuntimeException())
                );
    }

    /**
     * Handle the update of a recipe based on the provided request and existing recipe.
     *
     * @param request The update request containing the new recipe data.
     * @param recipe  The existing recipe to be updated.
     * @return ChangeResponse indicating the success or failure of the update operation.
     */
    private ChangeResponse handleUpdate(UpdateRecipeRequest request, Recipe recipe, RecipeByCategory recipeByCategory) {
        recipeMapper.applyUpdates(request, recipe);
        recipeService.saveRecipe(recipe);

        handleCategory(request, recipeByCategory);
        handleTagUpdate(request, recipe);

        recipeEventPublisher.publishUpdated(recipe);

        return ChangeResponse.newBuilder().setSuccess(true).build();
    }

    /**
     * Handle the update of a recipe's category based on the provided request and existing {@link RecipeByCategory}.
     *
     * @param request          The update request containing the new recipe data.
     * @param recipeByCategory The existing recipe by category to be updated.
     */
    private void handleCategory(UpdateRecipeRequest request, RecipeByCategory recipeByCategory) {
        recipeMapper.applyUpdates(request, recipeByCategory);

        if (request.hasCategory()) recipeByCategoryService.deleteRecipe(request.getId());
        recipeByCategoryService.saveRecipe(recipeByCategory);
    }

    /**
     * Handle the update of a recipe's tags. If tags are provided in the update,
     * delete old tag entries and insert new ones.
     *
     * @param request The update request containing the new recipe data.
     * @param recipe  The updated recipe (already has new tags applied).
     */
    private void handleTagUpdate(UpdateRecipeRequest request, Recipe recipe) {
        if (request.getTagsCount() > 0) {
            recipeByTagService.deleteByRecipeId(request.getId());
            recipeByTagService.saveAll(recipeMapper.toByTags(recipe));
        }
    }

    /**
     * Get a recipe by its ID.
     *
     * @param request          Request containing the ID of the recipe to retrieve.
     * @param responseObserver StreamObserver to send the response back to the client.
     */
    @Override
    public void getRecipeById(GetRecipeByIdRequest request, StreamObserver<GetRecipeByIdResponse> responseObserver) {
        if (request.getId().isBlank()) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription("Recipe ID must not be empty").asRuntimeException());
            return;
        }

        recipeService.getRecipeById(request.getId())
                .ifPresentOrElse(
                        recipe -> {
                            responseObserver.onNext(recipeMapper.toGetResponse(recipe));
                            responseObserver.onCompleted();
                        },
                        () -> responseObserver.onError(
                                Status.NOT_FOUND
                                        .withDescription("Recipe not found: " + request.getId())
                                        .asRuntimeException())
                );
    }

    /**
     * Get recipes by category with pagination support.
     * Uses the {@code amount} and {@code site} fields from the request for page size and page number.
     *
     * @param request          Request containing the category and pagination parameters.
     * @param responseObserver StreamObserver to send the response back to the client.
     */
    @Override
    public void getRecipesByCategory(GetRecipesByCategoryRequest request, StreamObserver<GetRecipesByCategoryResponse> responseObserver) {
        if (request.getCategory().isBlank()) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription("Category must not be empty").asRuntimeException());
            return;
        }

        if (request.getAmount() > 0) {
            // Paginated query
            Slice<RecipeByCategory> slice = recipeByCategoryService.getRecipesByCategory(
                    request.getCategory(),
                    request.getAmount(),
                    request.getSite()
            );
            responseObserver.onNext(recipeMapper.toGetResponse(slice.getContent()));
        } else {
            // Unpaginated fallback
            responseObserver.onNext(
                    recipeMapper.toGetResponse(
                            recipeByCategoryService.getRecipesByCategory(request.getCategory())
                    )
            );
        }
        responseObserver.onCompleted();
    }

    /**
     * Get recipes by tag.
     *
     * @param request          Request containing the tag to search for.
     * @param responseObserver StreamObserver to send the response back to the client.
     */
    @Override
    public void getRecipesByTag(GetRecipesByTagRequest request, StreamObserver<GetRecipesByTagResponse> responseObserver) {
        if (request.getTag().isBlank()) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription("Tag must not be empty").asRuntimeException());
            return;
        }

        responseObserver.onNext(
                recipeMapper.toTagGetResponse(
                        recipeByTagService.getRecipesByTag(request.getTag())
                )
        );
        responseObserver.onCompleted();
    }

    // --- Validation ---

    /**
     * Validate a CreateRecipeRequest.
     *
     * @param request The request to validate.
     * @return A gRPC Status if validation fails, or null if valid.
     */
    private Status validateCreateRequest(CreateRecipeRequest request) {
        if (request.getTitle().isBlank()) {
            return Status.INVALID_ARGUMENT.withDescription("Title must not be empty");
        }
        if (request.getCategory().isBlank()) {
            return Status.INVALID_ARGUMENT.withDescription("Category must not be empty");
        }
        if (request.getDifficulty() < 1 || request.getDifficulty() > 5) {
            return Status.INVALID_ARGUMENT.withDescription("Difficulty must be between 1 and 5");
        }
        if (request.getServings() < 1) {
            return Status.INVALID_ARGUMENT.withDescription("Servings must be at least 1");
        }
        if (request.getCookingTimeMinutes() < 1) {
            return Status.INVALID_ARGUMENT.withDescription("Cooking time must be at least 1 minute");
        }
        if (request.getStepsCount() < 1) {
            return Status.INVALID_ARGUMENT.withDescription("At least one step is required");
        }
        return null;
    }

    /**
     * Validate an UpdateRecipeRequest (only validates fields that are present).
     *
     * @param request The request to validate.
     * @return A gRPC Status if validation fails, or null if valid.
     */
    private Status validateUpdateRequest(UpdateRecipeRequest request) {
        if (request.hasDifficulty() && (request.getDifficulty() < 1 || request.getDifficulty() > 5)) {
            return Status.INVALID_ARGUMENT.withDescription("Difficulty must be between 1 and 5");
        }
        if (request.hasServings() && request.getServings() < 1) {
            return Status.INVALID_ARGUMENT.withDescription("Servings must be at least 1");
        }
        if (request.hasCookingTimeMinutes() && request.getCookingTimeMinutes() < 1) {
            return Status.INVALID_ARGUMENT.withDescription("Cooking time must be at least 1 minute");
        }
        if (request.hasTitle() && request.getTitle().isBlank()) {
            return Status.INVALID_ARGUMENT.withDescription("Title must not be empty");
        }
        if (request.hasCategory() && request.getCategory().isBlank()) {
            return Status.INVALID_ARGUMENT.withDescription("Category must not be empty");
        }
        return null;
    }

}
