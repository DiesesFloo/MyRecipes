package eu.diesesfloo.myrecipes.recipes.recipe;

import eu.diesesfloo.myrecipes.recipes.event.RecipeEventPublisher;
import eu.diesesfloo.myrecipes.recipes.proto.*;
import eu.diesesfloo.myrecipes.recipes.recipe.category.RecipeByCategory;
import eu.diesesfloo.myrecipes.recipes.recipe.category.RecipeByCategoryKey;
import eu.diesesfloo.myrecipes.recipes.recipe.category.RecipeByCategoryService;
import eu.diesesfloo.myrecipes.recipes.recipe.tag.RecipeByTag;
import eu.diesesfloo.myrecipes.recipes.recipe.tag.RecipeByTagKey;
import eu.diesesfloo.myrecipes.recipes.recipe.tag.RecipeByTagService;
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
import org.springframework.data.domain.SliceImpl;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeGrpcServiceTest {

    @Mock
    private RecipeService recipeService;

    @Mock
    private RecipeByCategoryService recipeByCategoryService;

    @Mock
    private RecipeByTagService recipeByTagService;

    @Mock
    private RecipeEventPublisher recipeEventPublisher;

    @Spy
    @SuppressWarnings("unused")
    private RecipeMapper recipeMapper = new RecipeMapper();

    @InjectMocks
    private RecipeGrpcService grpcService;

    private CreateRecipeRequest validCreateRequest;

    @BeforeEach
    void setUp() {
        validCreateRequest = CreateRecipeRequest.newBuilder()
                .setTitle("Pasta Carbonara")
                .setCategory("Italian")
                .setDifficulty(3)
                .setServings(4)
                .setCookingTimeMinutes(20)
                .setCalories(600)
                .setProteins(25)
                .addAllIngredients(List.of("Pasta", "Eggs", "Bacon"))
                .addAllTags(List.of("dinner", "italian"))
                .addSteps(RecipeStep.newBuilder().setType(RecipeStepType.DEFAULT).setInstruction("Boil pasta").build())
                .build();
    }

    @Nested
    @DisplayName("AddRecipe Tests")
    class AddRecipeTests {

        @Test
        @DisplayName("Should successfully add recipe, save across tables, and publish Kafka event")
        void testAddRecipe_Success() {
            @SuppressWarnings("unchecked")
            StreamObserver<ChangeResponse> observer = mock(StreamObserver.class);

            grpcService.addRecipe(validCreateRequest, observer);

            verify(recipeService).saveRecipe(any(Recipe.class));
            verify(recipeByCategoryService).saveRecipe(any(RecipeByCategory.class));
            verify(recipeByTagService).saveAll(any());
            verify(recipeEventPublisher).publishCreated(any(Recipe.class));

            ArgumentCaptor<ChangeResponse> responseCaptor = ArgumentCaptor.forClass(ChangeResponse.class);
            verify(observer).onNext(responseCaptor.capture());
            assertTrue(responseCaptor.getValue().getSuccess());
            verify(observer).onCompleted();
            verify(observer, never()).onError(any());
        }

        @Test
        @DisplayName("Should fail with INVALID_ARGUMENT when title is empty")
        void testAddRecipe_EmptyTitle() {
            @SuppressWarnings("unchecked")
            StreamObserver<ChangeResponse> observer = mock(StreamObserver.class);
            CreateRecipeRequest req = validCreateRequest.toBuilder().setTitle("").build();

            grpcService.addRecipe(req, observer);

            ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
            verify(observer).onError(errorCaptor.capture());
            assertInstanceOf(StatusRuntimeException.class, errorCaptor.getValue());
            StatusRuntimeException ex = (StatusRuntimeException) errorCaptor.getValue();
            assertEquals(Status.Code.INVALID_ARGUMENT, ex.getStatus().getCode());
            assertTrue(ex.getStatus().getDescription().contains("Title"));
        }

        @Test
        @DisplayName("Should fail with INVALID_ARGUMENT when difficulty is out of range")
        void testAddRecipe_InvalidDifficulty() {
            @SuppressWarnings("unchecked")
            StreamObserver<ChangeResponse> observer = mock(StreamObserver.class);
            CreateRecipeRequest req = validCreateRequest.toBuilder().setDifficulty(6).build();

            grpcService.addRecipe(req, observer);

            ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
            verify(observer).onError(errorCaptor.capture());
            StatusRuntimeException ex = (StatusRuntimeException) errorCaptor.getValue();
            assertEquals(Status.Code.INVALID_ARGUMENT, ex.getStatus().getCode());
            assertTrue(ex.getStatus().getDescription().contains("Difficulty"));
        }

        @Test
        @DisplayName("Should fail with INVALID_ARGUMENT when steps are empty")
        void testAddRecipe_EmptySteps() {
            @SuppressWarnings("unchecked")
            StreamObserver<ChangeResponse> observer = mock(StreamObserver.class);
            CreateRecipeRequest req = validCreateRequest.toBuilder().clearSteps().build();

            grpcService.addRecipe(req, observer);

            ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
            verify(observer).onError(errorCaptor.capture());
            StatusRuntimeException ex = (StatusRuntimeException) errorCaptor.getValue();
            assertEquals(Status.Code.INVALID_ARGUMENT, ex.getStatus().getCode());
            assertTrue(ex.getStatus().getDescription().contains("step"));
        }
    }

    @Nested
    @DisplayName("GetRecipeById Tests")
    class GetRecipeByIdTests {

        @Test
        @DisplayName("Should return recipe when found")
        void testGetRecipeById_Found() {
            UUID id = UUID.randomUUID();
            Recipe recipe = Recipe.builder()
                    .id(id)
                    .title("Pizza")
                    .category("Italian")
                    .steps(List.of())
                    .build();

            when(recipeService.getRecipeById(id.toString())).thenReturn(Optional.of(recipe));

            @SuppressWarnings("unchecked")
            StreamObserver<GetRecipeByIdResponse> observer = mock(StreamObserver.class);
            GetRecipeByIdRequest req = GetRecipeByIdRequest.newBuilder().setId(id.toString()).build();

            grpcService.getRecipeById(req, observer);

            ArgumentCaptor<GetRecipeByIdResponse> responseCaptor = ArgumentCaptor.forClass(GetRecipeByIdResponse.class);
            verify(observer).onNext(responseCaptor.capture());
            assertEquals("Pizza", responseCaptor.getValue().getTitle());
            assertEquals(id.toString(), responseCaptor.getValue().getId());
            verify(observer).onCompleted();
        }

        @Test
        @DisplayName("Should return NOT_FOUND when recipe does not exist")
        void testGetRecipeById_NotFound() {
            String id = UUID.randomUUID().toString();
            when(recipeService.getRecipeById(id)).thenReturn(Optional.empty());

            @SuppressWarnings("unchecked")
            StreamObserver<GetRecipeByIdResponse> observer = mock(StreamObserver.class);
            GetRecipeByIdRequest req = GetRecipeByIdRequest.newBuilder().setId(id).build();

            grpcService.getRecipeById(req, observer);

            ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
            verify(observer).onError(errorCaptor.capture());
            StatusRuntimeException ex = (StatusRuntimeException) errorCaptor.getValue();
            assertEquals(Status.Code.NOT_FOUND, ex.getStatus().getCode());
        }

        @Test
        @DisplayName("Should return INVALID_ARGUMENT when ID is empty")
        void testGetRecipeById_EmptyId() {
            @SuppressWarnings("unchecked")
            StreamObserver<GetRecipeByIdResponse> observer = mock(StreamObserver.class);
            GetRecipeByIdRequest req = GetRecipeByIdRequest.newBuilder().setId("").build();

            grpcService.getRecipeById(req, observer);

            ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
            verify(observer).onError(errorCaptor.capture());
            StatusRuntimeException ex = (StatusRuntimeException) errorCaptor.getValue();
            assertEquals(Status.Code.INVALID_ARGUMENT, ex.getStatus().getCode());
        }
    }

    @Nested
    @DisplayName("DeleteRecipe Tests")
    class DeleteRecipeTests {

        @Test
        @DisplayName("Should delete recipe from all stores and publish Kafka event")
        void testDeleteRecipe_Success() {
            String id = UUID.randomUUID().toString();
            DeleteRecipeRequest req = DeleteRecipeRequest.newBuilder().setUuid(id).build();

            @SuppressWarnings("unchecked")
            StreamObserver<ChangeResponse> observer = mock(StreamObserver.class);

            grpcService.deleteRecipe(req, observer);

            verify(recipeService).deleteRecipe(id);
            verify(recipeByCategoryService).deleteRecipe(id);
            verify(recipeByTagService).deleteByRecipeId(id);
            verify(recipeEventPublisher).publishDeleted(id);

            verify(observer).onNext(any(ChangeResponse.class));
            verify(observer).onCompleted();
        }

        @Test
        @DisplayName("Should return INVALID_ARGUMENT when delete ID is empty")
        void testDeleteRecipe_EmptyId() {
            DeleteRecipeRequest req = DeleteRecipeRequest.newBuilder().setUuid("").build();
            @SuppressWarnings("unchecked")
            StreamObserver<ChangeResponse> observer = mock(StreamObserver.class);

            grpcService.deleteRecipe(req, observer);

            ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
            verify(observer).onError(errorCaptor.capture());
            StatusRuntimeException ex = (StatusRuntimeException) errorCaptor.getValue();
            assertEquals(Status.Code.INVALID_ARGUMENT, ex.getStatus().getCode());
        }
    }

    @Nested
    @DisplayName("UpdateRecipe Tests")
    class UpdateRecipeTests {

        @Test
        @DisplayName("Should update recipe and publish Kafka event")
        void testUpdateRecipe_Success() {
            UUID id = UUID.randomUUID();
            Recipe existing = Recipe.builder()
                    .id(id)
                    .title("Old Title")
                    .category("Italian")
                    .steps(List.of())
                    .build();

            RecipeByCategory categoryEntry = RecipeByCategory.builder()
                    .key(RecipeByCategoryKey.builder().category("Italian").id(id).build())
                    .title("Old Title")
                    .build();

            when(recipeService.getRecipeById(id.toString())).thenReturn(Optional.of(existing));
            when(recipeByCategoryService.getById(id.toString())).thenReturn(categoryEntry);

            UpdateRecipeRequest req = UpdateRecipeRequest.newBuilder()
                    .setId(id.toString())
                    .setTitle("New Title")
                    .build();

            @SuppressWarnings("unchecked")
            StreamObserver<ChangeResponse> observer = mock(StreamObserver.class);

            grpcService.updateRecipe(req, observer);

            verify(recipeService).saveRecipe(existing);
            verify(recipeEventPublisher).publishUpdated(existing);
            assertEquals("New Title", existing.getTitle());

            verify(observer).onNext(any(ChangeResponse.class));
            verify(observer).onCompleted();
        }

        @Test
        @DisplayName("Should return NOT_FOUND when updating non-existent recipe")
        void testUpdateRecipe_NotFound() {
            String id = UUID.randomUUID().toString();
            when(recipeService.getRecipeById(id)).thenReturn(Optional.empty());

            UpdateRecipeRequest req = UpdateRecipeRequest.newBuilder()
                    .setId(id)
                    .setTitle("New Title")
                    .build();

            @SuppressWarnings("unchecked")
            StreamObserver<ChangeResponse> observer = mock(StreamObserver.class);

            grpcService.updateRecipe(req, observer);

            ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
            verify(observer).onError(errorCaptor.capture());
            StatusRuntimeException ex = (StatusRuntimeException) errorCaptor.getValue();
            assertEquals(Status.Code.NOT_FOUND, ex.getStatus().getCode());
        }
    }

    @Nested
    @DisplayName("Category & Tag Search Tests")
    class SearchTests {

        @Test
        @DisplayName("Should return paginated recipes by category when amount > 0")
        void testGetRecipesByCategory_Paginated() {
            RecipeByCategory item = RecipeByCategory.builder()
                    .key(RecipeByCategoryKey.builder().category("Italian").id(UUID.randomUUID()).build())
                    .title("Lasagna")
                    .build();

            when(recipeByCategoryService.getRecipesByCategory("Italian", 5, 0))
                    .thenReturn(new SliceImpl<>(List.of(item)));

            GetRecipesByCategoryRequest req = GetRecipesByCategoryRequest.newBuilder()
                    .setCategory("Italian")
                    .setAmount(5)
                    .setSite(0)
                    .build();

            @SuppressWarnings("unchecked")
            StreamObserver<GetRecipesByCategoryResponse> observer = mock(StreamObserver.class);

            grpcService.getRecipesByCategory(req, observer);

            ArgumentCaptor<GetRecipesByCategoryResponse> captor = ArgumentCaptor.forClass(GetRecipesByCategoryResponse.class);
            verify(observer).onNext(captor.capture());
            assertEquals(1, captor.getValue().getRecipesCount());
            assertEquals("Lasagna", captor.getValue().getRecipes(0).getTitle());
            verify(observer).onCompleted();
        }

        @Test
        @DisplayName("Should return unpaginated recipes by category when amount == 0")
        void testGetRecipesByCategory_Unpaginated() {
            RecipeByCategory item = RecipeByCategory.builder()
                    .key(RecipeByCategoryKey.builder().category("Italian").id(UUID.randomUUID()).build())
                    .title("Lasagna")
                    .build();

            when(recipeByCategoryService.getRecipesByCategory("Italian"))
                    .thenReturn(Set.of(item));

            GetRecipesByCategoryRequest req = GetRecipesByCategoryRequest.newBuilder()
                    .setCategory("Italian")
                    .setAmount(0)
                    .build();

            @SuppressWarnings("unchecked")
            StreamObserver<GetRecipesByCategoryResponse> observer = mock(StreamObserver.class);

            grpcService.getRecipesByCategory(req, observer);

            ArgumentCaptor<GetRecipesByCategoryResponse> captor = ArgumentCaptor.forClass(GetRecipesByCategoryResponse.class);
            verify(observer).onNext(captor.capture());
            assertEquals(1, captor.getValue().getRecipesCount());
            assertEquals("Lasagna", captor.getValue().getRecipes(0).getTitle());
            verify(observer).onCompleted();
        }

        @Test
        @DisplayName("Should retrieve recipes by tag")
        void testGetRecipesByTag() {
            RecipeByTag item = RecipeByTag.builder()
                    .key(RecipeByTagKey.builder().tag("vegan").id(UUID.randomUUID()).build())
                    .title("Buddha Bowl")
                    .category("Healthy")
                    .build();

            when(recipeByTagService.getRecipesByTag("vegan")).thenReturn(Set.of(item));

            GetRecipesByTagRequest req = GetRecipesByTagRequest.newBuilder().setTag("vegan").build();

            @SuppressWarnings("unchecked")
            StreamObserver<GetRecipesByTagResponse> observer = mock(StreamObserver.class);

            grpcService.getRecipesByTag(req, observer);

            ArgumentCaptor<GetRecipesByTagResponse> captor = ArgumentCaptor.forClass(GetRecipesByTagResponse.class);
            verify(observer).onNext(captor.capture());
            assertEquals(1, captor.getValue().getRecipesCount());
            assertEquals("Buddha Bowl", captor.getValue().getRecipes(0).getTitle());
            verify(observer).onCompleted();
        }

        @Test
        @DisplayName("Should fail with INVALID_ARGUMENT when tag is empty")
        void testGetRecipesByTag_Empty() {
            GetRecipesByTagRequest req = GetRecipesByTagRequest.newBuilder().setTag("").build();

            @SuppressWarnings("unchecked")
            StreamObserver<GetRecipesByTagResponse> observer = mock(StreamObserver.class);

            grpcService.getRecipesByTag(req, observer);

            ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
            verify(observer).onError(captor.capture());
            StatusRuntimeException ex = (StatusRuntimeException) captor.getValue();
            assertEquals(Status.Code.INVALID_ARGUMENT, ex.getStatus().getCode());
        }
    }
}
