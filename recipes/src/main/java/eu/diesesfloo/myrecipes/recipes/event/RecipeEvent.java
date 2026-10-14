package eu.diesesfloo.myrecipes.recipes.event;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Event payload published to Kafka when a recipe is created, updated, or deleted.
 * Contains all recipe data needed by downstream consumers (User Service, Recommendation Service).
 */
@Data
@Builder
public class RecipeEvent {

    private UUID recipeId;

    private RecipeEventType eventType;

    private Instant timestamp;

    // Recipe data — null for DELETE events
    private String title;

    private String category;

    private Set<String> tags;

    private Set<String> ingredients;

    private int difficulty;

    private int cookingTimeMinutes;

    private int calories;

    private int proteins;

}
