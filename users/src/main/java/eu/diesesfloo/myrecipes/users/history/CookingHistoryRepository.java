package eu.diesesfloo.myrecipes.users.history;

import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.List;
import java.util.UUID;

/**
 * Cassandra repository for managing user cooking history.
 */
public interface CookingHistoryRepository extends CassandraRepository<CookingHistory, CookingHistoryKey> {

    List<CookingHistory> findByKeyUserId(UUID userId);

}
