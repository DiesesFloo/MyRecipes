package eu.diesesfloo.myrecipes.users.user;

import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.UUID;

/**
 * Repository interface for managing User entities in Cassandra.
 */
public interface UserRepository extends CassandraRepository<User, UUID> {

}
