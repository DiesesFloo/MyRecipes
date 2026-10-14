package eu.diesesfloo.myrecipes.users.event;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published to Kafka when a user interacts with a recipe
 * (cooked, rated, favorited, unfavorited).
 * The Recommendation Service consumes these events to build collaborative filtering matrices.
 */
@Data
@Builder
public class UserActivityEvent {

    private UUID userId;

    private UUID recipeId;

    private UserEventType eventType;

    private Integer rating;

    private Instant timestamp;

}
