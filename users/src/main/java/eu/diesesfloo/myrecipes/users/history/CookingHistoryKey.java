package eu.diesesfloo.myrecipes.users.history;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.data.cassandra.core.cql.PrimaryKeyType.CLUSTERED;
import static org.springframework.data.cassandra.core.cql.PrimaryKeyType.PARTITIONED;

/**
 * Composite primary key for cooking history table.
 * Partitioned by user, clustered by cookedAt descending for chronological retrieval.
 */
@Data
@Builder
@PrimaryKeyClass
public class CookingHistoryKey {

    @PrimaryKeyColumn(type = PARTITIONED)
    private UUID userId;

    @PrimaryKeyColumn(type = CLUSTERED, ordering = Ordering.DESCENDING)
    private Instant cookedAt;

    @PrimaryKeyColumn(type = CLUSTERED)
    private UUID recipeId;

}
