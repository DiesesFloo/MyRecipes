package eu.diesesfloo.myrecipes.recipes.recipe.category;

import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.Set;
import java.util.UUID;

/**
 * Repository interface for managing RecipeByCategory entities in Cassandra.
 */
public interface RecipeByCategoryRepository extends CassandraRepository<RecipeByCategory, RecipeByCategoryKey> {

    Set<RecipeByCategory> findByKeyCategory(String category);

    void deleteByKeyId(UUID id);

    RecipeByCategory findByKeyId(UUID id);
}
