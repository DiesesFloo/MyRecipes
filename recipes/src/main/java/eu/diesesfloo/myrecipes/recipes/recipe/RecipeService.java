package eu.diesesfloo.myrecipes.recipes.recipe;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Service class for managing recipes.
 */
@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;

    /**
     * Get a recipe by its ID.
     *
     * @param id The ID of the recipe to retrieve.
     * @return An Optional containing the recipe if found, or an empty Optional if not found.
     */
    @Cacheable(value = "recipes_by_id", key = "#id", unless = "#result.empty()")
    public Optional<Recipe> getRecipeById(String id) {
        return recipeRepository.findById(UUID.fromString(id));
    }

    /**
     * Save a recipe to the repository.
     *
     * @param recipe The recipe to save.
     */
    @CacheEvict(value = "recipes_by_id", key = "#recipe.id")
    public void saveRecipe(Recipe recipe) {
        recipeRepository.save(recipe);
    }

    /**
     * Delete a recipe by its ID.
     *
     * @param id The ID of the recipe to delete.
     */
    @CacheEvict(value = "recipes_by_id", key = "#id")
    public void deleteRecipe(String id) {
        recipeRepository.deleteById(UUID.fromString(id));
    }

}
