package eu.diesesfloo.myrecipes.recipes.recipe.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

/**
 * Service class for managing recipes by category. This service provides methods to retrieve,
 * add, and delete recipes based on their category.
 */
@Service
@RequiredArgsConstructor
public class RecipeByCategoryService {

    //TODO: Add pages functionality

    private final RecipeByCategoryRepository recipeRepository;

    /**
     * Get a recipe by its ID.
     *
     * @param category The category of the recipes to retrieve.
     * @return A Set containing the recipes with the provided category
     */
    public Set<RecipeByCategory> getRecipesByCategory(String category) {
        return recipeRepository.findByKeyCategory(category);
    }

    /**
     * Save a recipe to the repository.
     *
     * @param recipe The recipe to save.
     */
    public void saveRecipe(RecipeByCategory recipe) {
        recipeRepository.save(recipe);
    }

    /**
     * Delete a recipe by its ID.
     *
     * @param id The ID of the recipe to delete.
     */
    public void deleteRecipe(String id) {
        recipeRepository.deleteByKeyId(UUID.fromString(id));
    }

    /**
     * Get a recipe by its ID.
     *
     * @param id The ID of the recipe to retrieve.
     * @return the recipe with the provided ID
     */
    public RecipeByCategory getById(String id) {
        return recipeRepository.findByKeyId(UUID.fromString(id));
    }

}
