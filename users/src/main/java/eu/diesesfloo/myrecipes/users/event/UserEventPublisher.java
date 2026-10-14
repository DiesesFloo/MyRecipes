package eu.diesesfloo.myrecipes.users.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Publishes user activity events to the "user-activities" Kafka topic.
 * Keyed by userId for partition ordering.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {

    private static final String TOPIC = "user-activities";

    private final KafkaTemplate<String, UserActivityEvent> kafkaTemplate;

    public void publishRecipeCooked(UUID userId, UUID recipeId) {
        publish(UserActivityEvent.builder()
                .userId(userId)
                .recipeId(recipeId)
                .eventType(UserEventType.RECIPE_COOKED)
                .timestamp(Instant.now())
                .build());
    }

    public void publishRecipeRated(UUID userId, UUID recipeId, int rating) {
        publish(UserActivityEvent.builder()
                .userId(userId)
                .recipeId(recipeId)
                .eventType(UserEventType.RECIPE_RATED)
                .rating(rating)
                .timestamp(Instant.now())
                .build());
    }

    public void publishRecipeFavorited(UUID userId, UUID recipeId) {
        publish(UserActivityEvent.builder()
                .userId(userId)
                .recipeId(recipeId)
                .eventType(UserEventType.RECIPE_FAVORITED)
                .timestamp(Instant.now())
                .build());
    }

    public void publishRecipeUnfavorited(UUID userId, UUID recipeId) {
        publish(UserActivityEvent.builder()
                .userId(userId)
                .recipeId(recipeId)
                .eventType(UserEventType.RECIPE_UNFAVORITED)
                .timestamp(Instant.now())
                .build());
    }

    private void publish(UserActivityEvent event) {
        var _ = kafkaTemplate.send(TOPIC, event.getUserId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish user activity event: {}", event, ex);
                    } else {
                        log.info("Published {} event for user {} and recipe {}",
                                event.getEventType(), event.getUserId(), event.getRecipeId());
                    }
                });
    }

}
