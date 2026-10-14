package eu.diesesfloo.myrecipes.users.user;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Cassandra entity representing a user profile in the {@code users_by_id} table.
 */
@Table("users_by_id")
@Data
@Builder
public class User {

    @PrimaryKey
    private UUID id;

    private String username;

    private String email;

    private String displayName;

    private Set<String> dietaryPreferences;

    private String skillLevel;

    private Instant createdAt;

}
