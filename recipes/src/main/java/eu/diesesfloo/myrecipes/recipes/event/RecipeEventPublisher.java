package eu.diesesfloo.myrecipes.recipes.event;

import eu.diesesfloo.myrecipes.recipes.recipe.Recipe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Publishes recipe lifecycle events to Kafka.
 * All events go to the "recipe-events" topic, keyed by recipe ID for partition ordering.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RecipeEventPublisher {

    private static final String TOPIC = "recipe-events";

    private final KafkaTemplate<String, RecipeEvent> kafkaTemplate;

    /**
     * Publish a CREATED event for the given recipe.
     *
     * @param recipe The newly created recipe.
     */
    public void publishCreated(Recipe recipe) {
        publish(buildEvent(recipe, RecipeEventType.CREATED));
    }

    /**
     * Publish an UPDATED event for the given recipe.
     *
     * @param recipe The updated recipe.
     */
    public void publishUpdated(Recipe recipe) {
        publish(buildEvent(recipe, RecipeEventType.UPDATED));
    }

    /**
     * Publish a DELETED event for the given recipe ID.
     *
     * @param recipeId The ID of the deleted recipe (as string).
     */
    public void publishDeleted(String recipeId) {
        RecipeEvent event = RecipeEvent.builder()
                .recipeId(java.util.UUID.fromString(recipeId))
                .eventType(RecipeEventType.DELETED)
                .timestamp(Instant.now())
                .build();

        publish(event);
    }

    private RecipeEvent buildEvent(Recipe recipe, RecipeEventType type) {
        return RecipeEvent.builder()
                .recipeId(recipe.getId())
                .eventType(type)
                .timestamp(Instant.now())
                .title(recipe.getTitle())
                .category(recipe.getCategory())
                .tags(recipe.getTags())
                .ingredients(recipe.getIngredients())
                .difficulty(recipe.getDifficulty())
                .cookingTimeMinutes(recipe.getCookingTimeMinutes())
                .calories(recipe.getCalories())
                .proteins(recipe.getProteins())
                .build();
    }

    private void publish(RecipeEvent event) {
        var _ = kafkaTemplate.send(TOPIC, event.getRecipeId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish recipe event: {}", event, ex);
                    } else {
                        log.info("Published {} event for recipe {}", event.getEventType(), event.getRecipeId());
                    }
                });
    }

}
