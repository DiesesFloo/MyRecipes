package eu.diesesfloo.myrecipes.users.user;

import eu.diesesfloo.myrecipes.users.event.UserEventPublisher;
import eu.diesesfloo.myrecipes.users.favorite.UserFavorite;
import eu.diesesfloo.myrecipes.users.favorite.UserFavoriteKey;
import eu.diesesfloo.myrecipes.users.favorite.UserFavoriteService;
import eu.diesesfloo.myrecipes.users.history.CookingHistory;
import eu.diesesfloo.myrecipes.users.history.CookingHistoryKey;
import eu.diesesfloo.myrecipes.users.history.CookingHistoryService;
import eu.diesesfloo.myrecipes.users.proto.*;
import eu.diesesfloo.myrecipes.users.rating.RecipeRating;
import eu.diesesfloo.myrecipes.users.rating.RecipeRatingKey;
import eu.diesesfloo.myrecipes.users.rating.RecipeRatingService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserGrpcServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private CookingHistoryService cookingHistoryService;

    @Mock
    private RecipeRatingService recipeRatingService;

    @Mock
    private UserFavoriteService userFavoriteService;

    @Mock
    private UserEventPublisher userEventPublisher;

    @Spy
    @SuppressWarnings("unused")
    private UserMapper userMapper = new UserMapper();

    @InjectMocks
    private UserGrpcService grpcService;

    private CreateUserRequest validCreateRequest;

    @BeforeEach
    void setUp() {
        validCreateRequest = CreateUserRequest.newBuilder()
                .setUsername("culinary_master")
                .setEmail("master@food.com")
                .setDisplayName("Chef Gordon")
                .setSkillLevel("advanced")
                .addAllDietaryPreferences(List.of("keto"))
                .build();
    }

    @Nested
    @DisplayName("User CRUD Tests")
    class UserCrudTests {

        @Test
        @DisplayName("Should create user successfully")
        void testCreateUser_Success() {
            when(userService.saveUser(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            @SuppressWarnings("unchecked")
            StreamObserver<UserResponse> observer = mock(StreamObserver.class);

            grpcService.createUser(validCreateRequest, observer);

            ArgumentCaptor<UserResponse> captor = ArgumentCaptor.forClass(UserResponse.class);
            verify(observer).onNext(captor.capture());
            assertEquals("culinary_master", captor.getValue().getUsername());
            assertEquals("master@food.com", captor.getValue().getEmail());
            verify(observer).onCompleted();
        }

        @Test
        @DisplayName("Should fail when username is empty")
        void testCreateUser_EmptyUsername() {
            CreateUserRequest req = validCreateRequest.toBuilder().setUsername("").build();
            @SuppressWarnings("unchecked")
            StreamObserver<UserResponse> observer = mock(StreamObserver.class);

            grpcService.createUser(req, observer);

            ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
            verify(observer).onError(errorCaptor.capture());
            StatusRuntimeException ex = (StatusRuntimeException) errorCaptor.getValue();
            assertEquals(Status.Code.INVALID_ARGUMENT, ex.getStatus().getCode());
        }

        @Test
        @DisplayName("Should fail when email is invalid")
        void testCreateUser_InvalidEmail() {
            CreateUserRequest req = validCreateRequest.toBuilder().setEmail("not-an-email").build();
            @SuppressWarnings("unchecked")
            StreamObserver<UserResponse> observer = mock(StreamObserver.class);

            grpcService.createUser(req, observer);

            ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
            verify(observer).onError(errorCaptor.capture());
            StatusRuntimeException ex = (StatusRuntimeException) errorCaptor.getValue();
            assertEquals(Status.Code.INVALID_ARGUMENT, ex.getStatus().getCode());
        }

        @Test
        @DisplayName("Should retrieve user by ID")
        void testGetUserById_Success() {
            UUID id = UUID.randomUUID();
            User user = User.builder()
                    .id(id)
                    .username("john")
                    .email("john@test.com")
                    .build();

            when(userService.getUserById(id.toString())).thenReturn(Optional.of(user));

            @SuppressWarnings("unchecked")
            StreamObserver<UserResponse> observer = mock(StreamObserver.class);

            grpcService.getUserById(GetUserByIdRequest.newBuilder().setId(id.toString()).build(), observer);

            ArgumentCaptor<UserResponse> captor = ArgumentCaptor.forClass(UserResponse.class);
            verify(observer).onNext(captor.capture());
            assertEquals("john", captor.getValue().getUsername());
            verify(observer).onCompleted();
        }

        @Test
        @DisplayName("Should return NOT_FOUND when user does not exist")
        void testGetUserById_NotFound() {
            String id = UUID.randomUUID().toString();
            when(userService.getUserById(id)).thenReturn(Optional.empty());

            @SuppressWarnings("unchecked")
            StreamObserver<UserResponse> observer = mock(StreamObserver.class);

            grpcService.getUserById(GetUserByIdRequest.newBuilder().setId(id).build(), observer);

            ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
            verify(observer).onError(errorCaptor.capture());
            StatusRuntimeException ex = (StatusRuntimeException) errorCaptor.getValue();
            assertEquals(Status.Code.NOT_FOUND, ex.getStatus().getCode());
        }
    }

    @Nested
    @DisplayName("Cooking History Tests")
    class CookingHistoryTests {

        @Test
        @DisplayName("Should record cooking history and publish Kafka event")
        void testRecordCookingHistory_Success() {
            UUID userId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();
            CookingHistory history = CookingHistory.builder()
                    .key(CookingHistoryKey.builder().userId(userId).recipeId(recipeId).cookedAt(Instant.now()).build())
                    .recipeTitle("Curry")
                    .notes("Very spicy")
                    .build();

            when(cookingHistoryService.recordCooking(userId, recipeId, "Curry", "Very spicy")).thenReturn(history);

            RecordCookingHistoryRequest req = RecordCookingHistoryRequest.newBuilder()
                    .setUserId(userId.toString())
                    .setRecipeId(recipeId.toString())
                    .setRecipeTitle("Curry")
                    .setNotes("Very spicy")
                    .build();

            @SuppressWarnings("unchecked")
            StreamObserver<CookingHistoryResponse> observer = mock(StreamObserver.class);

            grpcService.recordCookingHistory(req, observer);

            verify(userEventPublisher).publishRecipeCooked(userId, recipeId);
            ArgumentCaptor<CookingHistoryResponse> captor = ArgumentCaptor.forClass(CookingHistoryResponse.class);
            verify(observer).onNext(captor.capture());
            assertTrue(captor.getValue().getSuccess());
            assertEquals("Curry", captor.getValue().getItem().getRecipeTitle());
            verify(observer).onCompleted();
        }
    }

    @Nested
    @DisplayName("Rating Tests")
    class RatingTests {

        @Test
        @DisplayName("Should rate recipe and publish Kafka event")
        void testRateRecipe_Success() {
            UUID userId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();
            RecipeRating rating = RecipeRating.builder()
                    .key(RecipeRatingKey.builder().userId(userId).recipeId(recipeId).build())
                    .rating(5)
                    .comment("Delicious!")
                    .ratedAt(Instant.now())
                    .build();

            when(recipeRatingService.rateRecipe(userId, recipeId, 5, "Delicious!")).thenReturn(rating);

            RateRecipeRequest req = RateRecipeRequest.newBuilder()
                    .setUserId(userId.toString())
                    .setRecipeId(recipeId.toString())
                    .setRating(5)
                    .setComment("Delicious!")
                    .build();

            @SuppressWarnings("unchecked")
            StreamObserver<RatingResponse> observer = mock(StreamObserver.class);

            grpcService.rateRecipe(req, observer);

            verify(userEventPublisher).publishRecipeRated(userId, recipeId, 5);
            ArgumentCaptor<RatingResponse> captor = ArgumentCaptor.forClass(RatingResponse.class);
            verify(observer).onNext(captor.capture());
            assertTrue(captor.getValue().getSuccess());
            assertEquals(5, captor.getValue().getRating().getRating());
            verify(observer).onCompleted();
        }

        @Test
        @DisplayName("Should fail when rating is out of 1-5 range")
        void testRateRecipe_InvalidRating() {
            UUID userId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();

            RateRecipeRequest req = RateRecipeRequest.newBuilder()
                    .setUserId(userId.toString())
                    .setRecipeId(recipeId.toString())
                    .setRating(6)
                    .build();

            @SuppressWarnings("unchecked")
            StreamObserver<RatingResponse> observer = mock(StreamObserver.class);

            grpcService.rateRecipe(req, observer);

            ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
            verify(observer).onError(errorCaptor.capture());
            StatusRuntimeException ex = (StatusRuntimeException) errorCaptor.getValue();
            assertEquals(Status.Code.INVALID_ARGUMENT, ex.getStatus().getCode());
        }
    }

    @Nested
    @DisplayName("Favorites Tests")
    class FavoritesTests {

        @Test
        @DisplayName("Should add favorite and publish Kafka event")
        void testAddFavorite_Success() {
            UUID userId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();

            FavoriteRequest req = FavoriteRequest.newBuilder()
                    .setUserId(userId.toString())
                    .setRecipeId(recipeId.toString())
                    .setRecipeTitle("Pizza")
                    .build();

            @SuppressWarnings("unchecked")
            StreamObserver<FavoriteResponse> observer = mock(StreamObserver.class);

            grpcService.addFavorite(req, observer);

            verify(userFavoriteService).addFavorite(userId, recipeId, "Pizza");
            verify(userEventPublisher).publishRecipeFavorited(userId, recipeId);

            ArgumentCaptor<FavoriteResponse> captor = ArgumentCaptor.forClass(FavoriteResponse.class);
            verify(observer).onNext(captor.capture());
            assertTrue(captor.getValue().getSuccess());
            verify(observer).onCompleted();
        }

        @Test
        @DisplayName("Should remove favorite and publish Kafka event")
        void testRemoveFavorite_Success() {
            UUID userId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();

            FavoriteRequest req = FavoriteRequest.newBuilder()
                    .setUserId(userId.toString())
                    .setRecipeId(recipeId.toString())
                    .build();

            @SuppressWarnings("unchecked")
            StreamObserver<FavoriteResponse> observer = mock(StreamObserver.class);

            grpcService.removeFavorite(req, observer);

            verify(userFavoriteService).removeFavorite(userId, recipeId);
            verify(userEventPublisher).publishRecipeUnfavorited(userId, recipeId);

            ArgumentCaptor<FavoriteResponse> captor = ArgumentCaptor.forClass(FavoriteResponse.class);
            verify(observer).onNext(captor.capture());
            assertTrue(captor.getValue().getSuccess());
            verify(observer).onCompleted();
        }

        @Test
        @DisplayName("Should get user favorites")
        void testGetFavorites_Success() {
            UUID userId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();
            UserFavorite fav = UserFavorite.builder()
                    .key(UserFavoriteKey.builder().userId(userId).recipeId(recipeId).build())
                    .recipeTitle("Tacos")
                    .addedAt(Instant.now())
                    .build();

            when(userFavoriteService.getFavorites(userId)).thenReturn(List.of(fav));

            GetFavoritesRequest req = GetFavoritesRequest.newBuilder().setUserId(userId.toString()).build();

            @SuppressWarnings("unchecked")
            StreamObserver<FavoritesResponse> observer = mock(StreamObserver.class);

            grpcService.getFavorites(req, observer);

            ArgumentCaptor<FavoritesResponse> captor = ArgumentCaptor.forClass(FavoritesResponse.class);
            verify(observer).onNext(captor.capture());
            assertEquals(1, captor.getValue().getFavoritesCount());
            assertEquals("Tacos", captor.getValue().getFavorites(0).getRecipeTitle());
            verify(observer).onCompleted();
        }
    }
}
