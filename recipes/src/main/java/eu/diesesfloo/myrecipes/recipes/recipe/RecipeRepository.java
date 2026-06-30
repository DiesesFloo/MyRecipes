package eu.diesesfloo.myrecipes.recipes.recipe;

import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.UUID;

/**
 * Repository interface for managing Recipe entities in Cassandra.
 */
public interface RecipeRepository extends CassandraRepository<Recipe, UUID> {

}
