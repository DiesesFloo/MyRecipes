package eu.diesesfloo.myrecipes.users.user;

import eu.diesesfloo.myrecipes.users.event.UserEventPublisher;
import eu.diesesfloo.myrecipes.users.favorite.UserFavorite;
import eu.diesesfloo.myrecipes.users.favorite.UserFavoriteService;
import eu.diesesfloo.myrecipes.users.history.CookingHistory;
import eu.diesesfloo.myrecipes.users.history.CookingHistoryService;
import eu.diesesfloo.myrecipes.users.proto.*;
import eu.diesesfloo.myrecipes.users.rating.RecipeRating;
import eu.diesesfloo.myrecipes.users.rating.RecipeRatingService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.grpc.server.service.GrpcService;

import java.util.List;
import java.util.UUID;

/**
 * gRPC service implementation for User operations, cooking history, recipe ratings, and favorites.
 */
@GrpcService
@RequiredArgsConstructor
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    private final UserService userService;
    private final UserMapper userMapper;
    private final CookingHistoryService cookingHistoryService;
    private final RecipeRatingService recipeRatingService;
    private final UserFavoriteService userFavoriteService;
    private final UserEventPublisher userEventPublisher;

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<UserResponse> responseObserver) {
        if (request.getUsername().isBlank()) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Username must not be empty").asRuntimeException());
            return;
        }
        if (request.getEmail().isBlank() || !request.getEmail().contains("@")) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("A valid email address is required").asRuntimeException());
            return;
        }

        User user = userMapper.toEntity(request);
        user = userService.saveUser(user);

        responseObserver.onNext(userMapper.toResponse(user));
        responseObserver.onCompleted();
    }

    @Override
    public void getUserById(GetUserByIdRequest request, StreamObserver<UserResponse> responseObserver) {
        if (request.getId().isBlank()) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("User ID must not be empty").asRuntimeException());
            return;
        }

        userService.getUserById(request.getId())
                .ifPresentOrElse(
                        user -> {
                            responseObserver.onNext(userMapper.toResponse(user));
                            responseObserver.onCompleted();
                        },
                        () -> responseObserver.onError(
                                Status.NOT_FOUND.withDescription("User not found: " + request.getId()).asRuntimeException())
                );
    }

    @Override
    public void updateUser(UpdateUserRequest request, StreamObserver<UserResponse> responseObserver) {
        if (request.getId().isBlank()) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("User ID must not be empty").asRuntimeException());
            return;
        }
        if (request.hasEmail() && (!request.getEmail().isBlank() && !request.getEmail().contains("@"))) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid email format").asRuntimeException());
            return;
        }

        userService.getUserById(request.getId())
                .ifPresentOrElse(
                        user -> {
                            userMapper.applyUpdates(request, user);
                            User updated = userService.saveUser(user);
                            responseObserver.onNext(userMapper.toResponse(updated));
                            responseObserver.onCompleted();
                        },
                        () -> responseObserver.onError(
                                Status.NOT_FOUND.withDescription("User not found: " + request.getId()).asRuntimeException())
                );
    }

    @Override
    public void recordCookingHistory(RecordCookingHistoryRequest request, StreamObserver<CookingHistoryResponse> responseObserver) {
        UUID userId = parseUuid(request.getUserId(), "User ID", responseObserver);
        if (userId == null) return;
        UUID recipeId = parseUuid(request.getRecipeId(), "Recipe ID", responseObserver);
        if (recipeId == null) return;

        CookingHistory history = cookingHistoryService.recordCooking(
                userId, recipeId, request.getRecipeTitle(), request.getNotes());

        userEventPublisher.publishRecipeCooked(userId, recipeId);

        CookingHistoryItem item = CookingHistoryItem.newBuilder()
                .setUserId(history.getKey().getUserId().toString())
                .setRecipeId(history.getKey().getRecipeId().toString())
                .setRecipeTitle(history.getRecipeTitle())
                .setNotes(history.getNotes())
                .setCookedAt(history.getKey().getCookedAt().toEpochMilli())
                .build();

        responseObserver.onNext(CookingHistoryResponse.newBuilder()
                .setSuccess(true)
                .setItem(item)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getCookingHistory(GetCookingHistoryRequest request, StreamObserver<CookingHistoryListResponse> responseObserver) {
        UUID userId = parseUuid(request.getUserId(), "User ID", responseObserver);
        if (userId == null) return;

        List<CookingHistory> historyList = cookingHistoryService.getHistory(userId, request.getLimit());

        List<CookingHistoryItem> items = historyList.stream()
                .map(h -> CookingHistoryItem.newBuilder()
                        .setUserId(h.getKey().getUserId().toString())
                        .setRecipeId(h.getKey().getRecipeId().toString())
                        .setRecipeTitle(h.getRecipeTitle())
                        .setNotes(h.getNotes())
                        .setCookedAt(h.getKey().getCookedAt().toEpochMilli())
                        .build())
                .toList();

        responseObserver.onNext(CookingHistoryListResponse.newBuilder()
                .addAllItems(items)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void rateRecipe(RateRecipeRequest request, StreamObserver<RatingResponse> responseObserver) {
        UUID userId = parseUuid(request.getUserId(), "User ID", responseObserver);
        if (userId == null) return;
        UUID recipeId = parseUuid(request.getRecipeId(), "Recipe ID", responseObserver);
        if (recipeId == null) return;

        if (request.getRating() < 1 || request.getRating() > 5) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Rating must be between 1 and 5 stars").asRuntimeException());
            return;
        }

        RecipeRating rating = recipeRatingService.rateRecipe(userId, recipeId, request.getRating(), request.getComment());

        userEventPublisher.publishRecipeRated(userId, recipeId, request.getRating());

        RecipeRatingItem item = RecipeRatingItem.newBuilder()
                .setUserId(rating.getKey().getUserId().toString())
                .setRecipeId(rating.getKey().getRecipeId().toString())
                .setRating(rating.getRating())
                .setComment(rating.getComment())
                .setRatedAt(rating.getRatedAt().toEpochMilli())
                .build();

        responseObserver.onNext(RatingResponse.newBuilder()
                .setSuccess(true)
                .setRating(item)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getUserRatings(GetUserRatingsRequest request, StreamObserver<UserRatingsResponse> responseObserver) {
        UUID userId = parseUuid(request.getUserId(), "User ID", responseObserver);
        if (userId == null) return;

        List<RecipeRating> ratings = recipeRatingService.getUserRatings(userId);

        List<RecipeRatingItem> items = ratings.stream()
                .map(r -> RecipeRatingItem.newBuilder()
                        .setUserId(r.getKey().getUserId().toString())
                        .setRecipeId(r.getKey().getRecipeId().toString())
                        .setRating(r.getRating())
                        .setComment(r.getComment())
                        .setRatedAt(r.getRatedAt().toEpochMilli())
                        .build())
                .toList();

        responseObserver.onNext(UserRatingsResponse.newBuilder()
                .addAllRatings(items)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void addFavorite(FavoriteRequest request, StreamObserver<FavoriteResponse> responseObserver) {
        UUID userId = parseUuid(request.getUserId(), "User ID", responseObserver);
        if (userId == null) return;
        UUID recipeId = parseUuid(request.getRecipeId(), "Recipe ID", responseObserver);
        if (recipeId == null) return;

        userFavoriteService.addFavorite(userId, recipeId, request.getRecipeTitle());
        userEventPublisher.publishRecipeFavorited(userId, recipeId);

        responseObserver.onNext(FavoriteResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void removeFavorite(FavoriteRequest request, StreamObserver<FavoriteResponse> responseObserver) {
        UUID userId = parseUuid(request.getUserId(), "User ID", responseObserver);
        if (userId == null) return;
        UUID recipeId = parseUuid(request.getRecipeId(), "Recipe ID", responseObserver);
        if (recipeId == null) return;

        userFavoriteService.removeFavorite(userId, recipeId);
        userEventPublisher.publishRecipeUnfavorited(userId, recipeId);

        responseObserver.onNext(FavoriteResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getFavorites(GetFavoritesRequest request, StreamObserver<FavoritesResponse> responseObserver) {
        UUID userId = parseUuid(request.getUserId(), "User ID", responseObserver);
        if (userId == null) return;

        List<UserFavorite> favorites = userFavoriteService.getFavorites(userId);

        List<FavoriteItem> items = favorites.stream()
                .map(f -> FavoriteItem.newBuilder()
                        .setUserId(f.getKey().getUserId().toString())
                        .setRecipeId(f.getKey().getRecipeId().toString())
                        .setRecipeTitle(f.getRecipeTitle())
                        .setAddedAt(f.getAddedAt().toEpochMilli())
                        .build())
                .toList();

        responseObserver.onNext(FavoritesResponse.newBuilder()
                .addAllFavorites(items)
                .build());
        responseObserver.onCompleted();
    }

    private <T> UUID parseUuid(String str, String fieldName, StreamObserver<T> responseObserver) {
        if (str == null || str.isBlank()) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(fieldName + " must not be empty").asRuntimeException());
            return null;
        }
        try {
            return UUID.fromString(str);
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(fieldName + " is not a valid UUID: " + str).asRuntimeException());
            return null;
        }
    }

}
