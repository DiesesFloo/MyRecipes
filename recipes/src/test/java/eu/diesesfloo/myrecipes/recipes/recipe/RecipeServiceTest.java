package eu.diesesfloo.myrecipes.recipes.recipe;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private RecipeService recipeService;

    @Test
    @DisplayName("Should find recipe by ID when it exists")
    void testGetRecipeById_Found() {
        UUID id = UUID.randomUUID();
        Recipe recipe = Recipe.builder().id(id).title("Pasta").build();

        when(recipeRepository.findById(id)).thenReturn(Optional.of(recipe));

        Optional<Recipe> result = recipeService.getRecipeById(id.toString());

        assertTrue(result.isPresent());
        assertEquals("Pasta", result.get().getTitle());
        verify(recipeRepository).findById(id);
    }

    @Test
    @DisplayName("Should return empty optional when recipe does not exist")
    void testGetRecipeById_NotFound() {
        UUID id = UUID.randomUUID();
        when(recipeRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Recipe> result = recipeService.getRecipeById(id.toString());

        assertTrue(result.isEmpty());
        verify(recipeRepository).findById(id);
    }

    @Test
    @DisplayName("Should save recipe via repository")
    void testSaveRecipe() {
        Recipe recipe = Recipe.builder().id(UUID.randomUUID()).title("Salad").build();

        recipeService.saveRecipe(recipe);

        verify(recipeRepository).save(recipe);
    }

    @Test
    @DisplayName("Should delete recipe by UUID")
    void testDeleteRecipe() {
        UUID id = UUID.randomUUID();

        recipeService.deleteRecipe(id.toString());

        verify(recipeRepository).deleteById(id);
    }
}
