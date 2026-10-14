package eu.diesesfloo.myrecipes.recipes.recipe.tag;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

/**
 * Service class for managing recipes by tag.
 * Handles the denormalized {@code recipes_by_tag} table operations.
 */
@Service
@RequiredArgsConstructor
public class RecipeByTagService {

    private final RecipeByTagRepository recipeByTagRepository;

    /**
     * Get all recipes with the given tag.
     *
     * @param tag The tag to search for.
     * @return A Set containing the recipes with the provided tag.
     */
    public Set<RecipeByTag> getRecipesByTag(String tag) {
        return recipeByTagRepository.findByKeyTag(tag);
    }

    /**
     * Save a recipe-tag association.
     *
     * @param recipeByTag The recipe-tag entry to save.
     */
    public void saveRecipe(RecipeByTag recipeByTag) {
        recipeByTagRepository.save(recipeByTag);
    }

    /**
     * Save multiple recipe-tag associations at once (for a recipe with multiple tags).
     *
     * @param recipes The recipe-tag entries to save.
     */
    public void saveAll(Set<RecipeByTag> recipes) {
        recipeByTagRepository.saveAll(recipes);
    }

    /**
     * Delete all tag entries for a given recipe ID.
     *
     * @param id The ID of the recipe whose tag entries should be deleted.
     */
    public void deleteByRecipeId(String id) {
        recipeByTagRepository.deleteByKeyId(UUID.fromString(id));
    }

}
