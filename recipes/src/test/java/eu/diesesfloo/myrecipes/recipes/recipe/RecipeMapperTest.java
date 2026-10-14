package eu.diesesfloo.myrecipes.recipes.recipe;

import eu.diesesfloo.myrecipes.recipes.proto.CreateRecipeRequest;
import eu.diesesfloo.myrecipes.recipes.proto.GetRecipeByIdResponse;
import eu.diesesfloo.myrecipes.recipes.proto.GetRecipesByCategoryResponse;
import eu.diesesfloo.myrecipes.recipes.proto.GetRecipesByTagResponse;
import eu.diesesfloo.myrecipes.recipes.proto.RecipeStep;
import eu.diesesfloo.myrecipes.recipes.proto.RecipeStepType;
import eu.diesesfloo.myrecipes.recipes.proto.UpdateRecipeRequest;
import eu.diesesfloo.myrecipes.recipes.recipe.category.RecipeByCategory;
import eu.diesesfloo.myrecipes.recipes.recipe.category.RecipeByCategoryKey;
import eu.diesesfloo.myrecipes.recipes.recipe.step.Step;
import eu.diesesfloo.myrecipes.recipes.recipe.step.StepType;
import eu.diesesfloo.myrecipes.recipes.recipe.tag.RecipeByTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RecipeMapperTest {

    private RecipeMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new RecipeMapper();
    }

    @Test
    @DisplayName("Should convert CreateRecipeRequest to Recipe domain model")
    void testRecipeFromCreateRequest() {
        CreateRecipeRequest request = CreateRecipeRequest.newBuilder()
                .setTitle("Spaghetti Carbonara")
                .setCategory("Italian")
                .setDescription("Classic roman pasta")
                .setCookingTimeMinutes(25)
                .setDifficulty(2)
                .setServings(4)
                .setCalories(650)
                .setProteins(28)
                .setImageUrl("https://example.com/carbonara.jpg")
                .addAllIngredients(List.of("Spaghetti", "Eggs", "Guanciale", "Pecorino", "Black Pepper"))
                .addAllTags(List.of("pasta", "quick", "dinner"))
                .addSteps(RecipeStep.newBuilder().setType(RecipeStepType.DEFAULT).setInstruction("Boil water").build())
                .addSteps(RecipeStep.newBuilder().setType(RecipeStepType.TIMER).setInstruction("Cook pasta").setTimer(600).build())
                .build();

        Recipe recipe = mapper.recipeFromCreateRequest(request);

        assertNotNull(recipe.getId());
        assertEquals("Spaghetti Carbonara", recipe.getTitle());
        assertEquals("Italian", recipe.getCategory());
        assertEquals(2, recipe.getDifficulty());
        assertEquals(4, recipe.getServings());
        assertEquals(25, recipe.getCookingTimeMinutes());
        assertEquals(650, recipe.getCalories());
        assertEquals(28, recipe.getProteins());
        assertEquals(5, recipe.getIngredients().size());
        assertEquals(3, recipe.getTags().size());
        assertEquals(2, recipe.getSteps().size());
        assertEquals(StepType.DEFAULT, recipe.getSteps().get(0).getType());
        assertEquals(StepType.TIMER, recipe.getSteps().get(1).getType());
        assertEquals(600, recipe.getSteps().get(1).getTimer());
    }

    @Test
    @DisplayName("Should map Recipe to GetRecipeByIdResponse")
    void testToGetResponse() {
        UUID id = UUID.randomUUID();
        Recipe recipe = Recipe.builder()
                .id(id)
                .title("Tacos")
                .category("Mexican")
                .description("Tasty beef tacos")
                .difficulty(1)
                .servings(2)
                .cookingTimeMinutes(15)
                .calories(450)
                .proteins(22)
                .imageUrl("https://example.com/tacos.jpg")
                .ingredients(Set.of("Tortillas", "Beef", "Salsa"))
                .tags(Set.of("mexican", "street-food"))
                .steps(List.of(
                        new Step("Warm tortillas"),
                        new Step("Cook beef", 300)
                ))
                .build();

        GetRecipeByIdResponse response = mapper.toGetResponse(recipe);

        assertEquals(id.toString(), response.getId());
        assertEquals("Tacos", response.getTitle());
        assertEquals("Mexican", response.getCategory());
        assertEquals(1, response.getDifficulty());
        assertEquals(2, response.getServings());
        assertEquals(15, response.getCookingTimeMinutes());
        assertEquals(450, response.getCalories());
        assertEquals(22, response.getProteins());
        assertEquals(3, response.getIngredientsCount());
        assertEquals(2, response.getTagsCount());
        assertEquals(2, response.getStepsCount());
        assertEquals(RecipeStepType.TIMER, response.getSteps(1).getType());
        assertEquals(300, response.getSteps(1).getTimer());
    }

    @Test
    @DisplayName("Should convert Recipe to RecipeByCategory")
    void testToByCategory() {
        UUID id = UUID.randomUUID();
        Recipe recipe = Recipe.builder()
                .id(id)
                .title("Sushi Roll")
                .category("Japanese")
                .difficulty(3)
                .cookingTimeMinutes(45)
                .calories(320)
                .proteins(14)
                .imageUrl("https://example.com/sushi.jpg")
                .build();

        RecipeByCategory byCategory = mapper.toByCategory(recipe);

        assertEquals("Japanese", byCategory.getKey().getCategory());
        assertEquals(id, byCategory.getKey().getId());
        assertEquals("Sushi Roll", byCategory.getTitle());
        assertEquals(3, byCategory.getDifficulty());
        assertEquals(45, byCategory.getCookingTimeMinutes());
    }

    @Test
    @DisplayName("Should convert Recipe to multiple RecipeByTag entries")
    void testToByTags() {
        UUID id = UUID.randomUUID();
        Recipe recipe = Recipe.builder()
                .id(id)
                .title("Salad")
                .category("Healthy")
                .tags(Set.of("vegan", "quick", "low-carb"))
                .difficulty(1)
                .cookingTimeMinutes(10)
                .calories(150)
                .proteins(4)
                .build();

        Set<RecipeByTag> tags = mapper.toByTags(recipe);

        assertEquals(3, tags.size());
        assertTrue(tags.stream().anyMatch(t -> "vegan".equals(t.getKey().getTag())));
        assertTrue(tags.stream().anyMatch(t -> "quick".equals(t.getKey().getTag())));
        assertTrue(tags.stream().anyMatch(t -> "low-carb".equals(t.getKey().getTag())));
        for (RecipeByTag tagEntry : tags) {
            assertEquals(id, tagEntry.getKey().getId());
            assertEquals("Healthy", tagEntry.getCategory());
            assertEquals("Salad", tagEntry.getTitle());
        }
    }

    @Test
    @DisplayName("Should convert List of RecipeByCategory to paginated response")
    void testToGetResponsePaginatedList() {
        RecipeByCategory r1 = RecipeByCategory.builder()
                .key(RecipeByCategoryKey.builder().category("Italian").id(UUID.randomUUID()).build())
                .title("Pizza Margherita")
                .difficulty(2)
                .build();
        RecipeByCategory r2 = RecipeByCategory.builder()
                .key(RecipeByCategoryKey.builder().category("Italian").id(UUID.randomUUID()).build())
                .title("Risotto")
                .difficulty(3)
                .build();

        GetRecipesByCategoryResponse response = mapper.toGetResponse(List.of(r1, r2));

        assertEquals(2, response.getRecipesCount());
        assertEquals("Pizza Margherita", response.getRecipes(0).getTitle());
        assertEquals("Risotto", response.getRecipes(1).getTitle());
    }

    @Test
    @DisplayName("Should apply partial updates correctly")
    void testApplyUpdates() {
        Recipe recipe = Recipe.builder()
                .id(UUID.randomUUID())
                .title("Old Title")
                .category("Old Category")
                .difficulty(1)
                .servings(2)
                .cookingTimeMinutes(10)
                .calories(200)
                .proteins(10)
                .description("Old Desc")
                .ingredients(Set.of("Ing1"))
                .tags(Set.of("tag1"))
                .steps(List.of(new Step("Step1")))
                .build();

        UpdateRecipeRequest req = UpdateRecipeRequest.newBuilder()
                .setId(recipe.getId().toString())
                .setTitle("New Title")
                .setDifficulty(4)
                .addAllIngredients(List.of("IngA", "IngB"))
                .build();

        mapper.applyUpdates(req, recipe);

        assertEquals("New Title", recipe.getTitle());
        assertEquals(4, recipe.getDifficulty());
        assertEquals("Old Category", recipe.getCategory()); // unchanged
        assertEquals(2, recipe.getServings()); // unchanged
        assertEquals(2, recipe.getIngredients().size());
        assertTrue(recipe.getIngredients().contains("IngA"));
    }
}
