package eu.diesesfloo.myrecipes.users.history;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * Denormalized cooking history record stored in Cassandra.
 */
@Table("cooking_history_by_user")
@Data
@Builder
public class CookingHistory {

    @PrimaryKey
    private CookingHistoryKey key;

    private String recipeTitle;

    private String notes;

}
