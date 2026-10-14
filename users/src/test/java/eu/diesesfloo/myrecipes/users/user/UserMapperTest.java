package eu.diesesfloo.myrecipes.users.user;

import eu.diesesfloo.myrecipes.users.proto.CreateUserRequest;
import eu.diesesfloo.myrecipes.users.proto.UpdateUserRequest;
import eu.diesesfloo.myrecipes.users.proto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private UserMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UserMapper();
    }

    @Test
    @DisplayName("Should convert CreateUserRequest to User entity")
    void testToEntity() {
        CreateUserRequest request = CreateUserRequest.newBuilder()
                .setUsername("johndoe")
                .setEmail("john@example.com")
                .setDisplayName("John Doe")
                .addAllDietaryPreferences(List.of("vegetarian", "gluten-free"))
                .setSkillLevel("intermediate")
                .build();

        User user = mapper.toEntity(request);

        assertNotNull(user.getId());
        assertEquals("johndoe", user.getUsername());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("John Doe", user.getDisplayName());
        assertEquals(2, user.getDietaryPreferences().size());
        assertEquals("INTERMEDIATE", user.getSkillLevel());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    @DisplayName("Should map User entity to UserResponse")
    void testToResponse() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        User user = User.builder()
                .id(id)
                .username("chef_alice")
                .email("alice@kitchen.com")
                .displayName("Alice In Kitchen")
                .dietaryPreferences(Set.of("vegan"))
                .skillLevel("ADVANCED")
                .createdAt(now)
                .build();

        UserResponse response = mapper.toResponse(user);

        assertEquals(id.toString(), response.getId());
        assertEquals("chef_alice", response.getUsername());
        assertEquals("alice@kitchen.com", response.getEmail());
        assertEquals("Alice In Kitchen", response.getDisplayName());
        assertEquals("ADVANCED", response.getSkillLevel());
        assertEquals(1, response.getDietaryPreferencesCount());
        assertEquals(now.toEpochMilli(), response.getCreatedAt());
    }

    @Test
    @DisplayName("Should apply partial updates correctly")
    void testApplyUpdates() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("original_user")
                .email("orig@example.com")
                .displayName("Original")
                .skillLevel("BEGINNER")
                .dietaryPreferences(Set.of("keto"))
                .build();

        UpdateUserRequest req = UpdateUserRequest.newBuilder()
                .setId(user.getId().toString())
                .setDisplayName("Updated Name")
                .setSkillLevel("expert")
                .addAllDietaryPreferences(List.of("vegan", "nut-free"))
                .build();

        mapper.applyUpdates(req, user);

        assertEquals("Updated Name", user.getDisplayName());
        assertEquals("EXPERT", user.getSkillLevel());
        assertEquals(2, user.getDietaryPreferences().size());
        assertEquals("orig@example.com", user.getEmail()); // unchanged
    }
}
