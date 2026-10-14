package eu.diesesfloo.myrecipes.users.favorite;

import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.List;
import java.util.UUID;

/**
 * Cassandra repository for managing user favorites.
 */
public interface UserFavoriteRepository extends CassandraRepository<UserFavorite, UserFavoriteKey> {

    List<UserFavorite> findByKeyUserId(UUID userId);

    void deleteByKeyUserIdAndKeyRecipeId(UUID userId, UUID recipeId);

}
