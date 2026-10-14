package eu.diesesfloo.myrecipes.recipes.recipe.tag;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeByTagServiceTest {

    @Mock
    private RecipeByTagRepository repository;

    @InjectMocks
    private RecipeByTagService service;

    @Test
    @DisplayName("Should retrieve recipes by tag")
    void testGetRecipesByTag() {
        RecipeByTag tagEntry = RecipeByTag.builder()
                .key(RecipeByTagKey.builder().tag("vegan").id(UUID.randomUUID()).build())
                .title("Vegan Burger")
                .build();

        when(repository.findByKeyTag("vegan")).thenReturn(Set.of(tagEntry));

        Set<RecipeByTag> result = service.getRecipesByTag("vegan");

        assertEquals(1, result.size());
        assertEquals("Vegan Burger", result.iterator().next().getTitle());
        verify(repository).findByKeyTag("vegan");
    }

    @Test
    @DisplayName("Should save single recipe tag entry")
    void testSaveRecipe() {
        RecipeByTag entry = RecipeByTag.builder()
                .key(RecipeByTagKey.builder().tag("keto").id(UUID.randomUUID()).build())
                .build();

        service.saveRecipe(entry);

        verify(repository).save(entry);
    }

    @Test
    @DisplayName("Should save multiple recipe tag entries")
    void testSaveAll() {
        RecipeByTag e1 = RecipeByTag.builder()
                .key(RecipeByTagKey.builder().tag("keto").id(UUID.randomUUID()).build())
                .build();
        RecipeByTag e2 = RecipeByTag.builder()
                .key(RecipeByTagKey.builder().tag("dairy-free").id(UUID.randomUUID()).build())
                .build();

        service.saveAll(Set.of(e1, e2));

        verify(repository).saveAll(Set.of(e1, e2));
    }

    @Test
    @DisplayName("Should delete tags by recipe id")
    void testDeleteByRecipeId() {
        UUID id = UUID.randomUUID();

        service.deleteByRecipeId(id.toString());

        verify(repository).deleteByKeyId(id);
    }
}
