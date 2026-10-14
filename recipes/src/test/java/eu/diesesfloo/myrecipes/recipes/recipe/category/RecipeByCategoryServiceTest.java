package eu.diesesfloo.myrecipes.recipes.recipe.category;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeByCategoryServiceTest {

    @Mock
    private RecipeByCategoryRepository recipeRepository;

    @Mock
    private CassandraTemplate cassandraTemplate;

    @InjectMocks
    private RecipeByCategoryService recipeByCategoryService;

    @Test
    @DisplayName("Should retrieve recipes by category unpaginated")
    void testGetRecipesByCategory_Unpaginated() {
        RecipeByCategory recipe = RecipeByCategory.builder()
                .key(RecipeByCategoryKey.builder().category("Italian").id(UUID.randomUUID()).build())
                .title("Pasta")
                .build();

        when(recipeRepository.findByKeyCategory("Italian")).thenReturn(Set.of(recipe));

        Set<RecipeByCategory> result = recipeByCategoryService.getRecipesByCategory("Italian");

        assertEquals(1, result.size());
        assertEquals("Pasta", result.iterator().next().getTitle());
        verify(recipeRepository).findByKeyCategory("Italian");
    }

    @Test
    @DisplayName("Should retrieve paginated recipes by category using slice")
    void testGetRecipesByCategory_Paginated() {
        RecipeByCategory recipe = RecipeByCategory.builder()
                .key(RecipeByCategoryKey.builder().category("Italian").id(UUID.randomUUID()).build())
                .title("Pizza")
                .build();

        Slice<RecipeByCategory> slice = new SliceImpl<>(List.of(recipe));
        when(cassandraTemplate.slice(any(Query.class), eq(RecipeByCategory.class))).thenReturn(slice);

        Slice<RecipeByCategory> result = recipeByCategoryService.getRecipesByCategory("Italian", 10, 0);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Pizza", result.getContent().get(0).getTitle());
        verify(cassandraTemplate).slice(any(Query.class), eq(RecipeByCategory.class));
    }

    @Test
    @DisplayName("Should save recipe by category")
    void testSaveRecipe() {
        RecipeByCategory recipe = RecipeByCategory.builder()
                .key(RecipeByCategoryKey.builder().category("Dessert").id(UUID.randomUUID()).build())
                .title("Tiramisu")
                .build();

        recipeByCategoryService.saveRecipe(recipe);

        verify(recipeRepository).save(recipe);
    }

    @Test
    @DisplayName("Should delete recipe by id")
    void testDeleteRecipe() {
        UUID id = UUID.randomUUID();

        recipeByCategoryService.deleteRecipe(id.toString());

        verify(recipeRepository).deleteByKeyId(id);
    }

    @Test
    @DisplayName("Should get recipe by id")
    void testGetById() {
        UUID id = UUID.randomUUID();
        RecipeByCategory recipe = RecipeByCategory.builder()
                .key(RecipeByCategoryKey.builder().category("Soup").id(id).build())
                .title("Minestrone")
                .build();

        when(recipeRepository.findByKeyId(id)).thenReturn(recipe);

        RecipeByCategory result = recipeByCategoryService.getById(id.toString());

        assertNotNull(result);
        assertEquals("Minestrone", result.getTitle());
    }
}
