package eu.diesesfloo.myrecipes.recipes.recipe.category;

import org.springframework.data.cassandra.repository.CassandraRepository;

/**
 * Repository interface for managing RecipeByCategory entities in Cassandra.
 */
public interface RecipeByCategoryRepository extends CassandraRepository<RecipeByCategory, RecipeByCategoryKey> {
}
