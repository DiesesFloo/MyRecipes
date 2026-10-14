package eu.diesesfloo.myrecipes.users.rating;

import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.List;
import java.util.UUID;

/**
 * Cassandra repository for managing recipe ratings.
 */
public interface RecipeRatingRepository extends CassandraRepository<RecipeRating, RecipeRatingKey> {

    List<RecipeRating> findByKeyUserId(UUID userId);

}
