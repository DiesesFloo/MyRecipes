package eu.diesesfloo.myrecipes.recipes.recipe.category;

import lombok.RequiredArgsConstructor;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.query.CassandraPageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

import static org.springframework.data.cassandra.core.query.Criteria.where;
import static org.springframework.data.cassandra.core.query.Query.query;

/**
 * Service class for managing recipes by category. This service provides methods to retrieve,
 * add, and delete recipes based on their category.
 */
@Service
@RequiredArgsConstructor
public class RecipeByCategoryService {

    private final RecipeByCategoryRepository recipeRepository;
    private final CassandraTemplate cassandraTemplate;

    /**
     * Get all recipes with the given category (unpaginated).
     *
     * @param category The category of the recipes to retrieve.
     * @return A Set containing the recipes with the provided category.
     */
    public Set<RecipeByCategory> getRecipesByCategory(String category) {
        return recipeRepository.findByKeyCategory(category);
    }

    /**
     * Get a paginated slice of recipes for the given category.
     *
     * @param category The category to query.
     * @param pageSize The number of results per page.
     * @param page     The page number (0-based).
     * @return A Slice of recipes for the requested page.
     */
    public Slice<RecipeByCategory> getRecipesByCategory(String category, int pageSize, int page) {
        CassandraPageRequest pageRequest = CassandraPageRequest.first(pageSize);

        Slice<RecipeByCategory> slice = cassandraTemplate.slice(
                query(where("category").is(category))
                        .pageRequest(pageRequest),
                RecipeByCategory.class
        );

        // Advance to the requested page
        int currentPage = 0;
        while (slice.hasNext() && currentPage < page) {
            slice = cassandraTemplate.slice(
                    query(where("category").is(category))
                            .pageRequest(slice.nextPageable()),
                    RecipeByCategory.class
            );
            currentPage++;
        }

        return slice;
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
     * @return the recipe with the provided ID.
     */
    public RecipeByCategory getById(String id) {
        return recipeRepository.findByKeyId(UUID.fromString(id));
    }

}
