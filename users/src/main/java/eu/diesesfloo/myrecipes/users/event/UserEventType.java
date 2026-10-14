package eu.diesesfloo.myrecipes.users.event;

/**
 * Event types representing user activities and interactions with recipes.
 * Consumed by the Recommendation Service for collaborative filtering.
 */
public enum UserEventType {

    RECIPE_COOKED,
    RECIPE_RATED,
    RECIPE_FAVORITED,
    RECIPE_UNFAVORITED

}
