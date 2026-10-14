package eu.diesesfloo.myrecipes.recipes.recipe.tag;

import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.Set;
import java.util.UUID;

/**
 * Repository interface for managing RecipeByTag entities in Cassandra.
 */
public interface RecipeByTagRepository extends CassandraRepository<RecipeByTag, RecipeByTagKey> {

    Set<RecipeByTag> findByKeyTag(String tag);

    void deleteByKeyId(UUID id);

}
