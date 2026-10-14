package eu.diesesfloo.myrecipes.users.user;

import eu.diesesfloo.myrecipes.users.proto.CreateUserRequest;
import eu.diesesfloo.myrecipes.users.proto.UpdateUserRequest;
import eu.diesesfloo.myrecipes.users.proto.UserResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashSet;
import java.util.Locale;
import java.util.UUID;

/**
 * Mapper for converting between User domain entities and gRPC protobuf messages.
 */
@Component
public class UserMapper {

    public User toEntity(CreateUserRequest request) {
        return User.builder()
                .id(UUID.randomUUID())
                .username(request.getUsername())
                .email(request.getEmail())
                .displayName(request.getDisplayName().isBlank() ? request.getUsername() : request.getDisplayName())
                .dietaryPreferences(new HashSet<>(request.getDietaryPreferencesList()))
                .skillLevel(request.getSkillLevel().isBlank() ? "BEGINNER" : request.getSkillLevel().toUpperCase(Locale.ROOT))
                .createdAt(Instant.now())
                .build();
    }

    public UserResponse toResponse(User user) {
        UserResponse.Builder builder = UserResponse.newBuilder()
                .setId(user.getId() != null ? user.getId().toString() : "")
                .setUsername(user.getUsername() != null ? user.getUsername() : "")
                .setEmail(user.getEmail() != null ? user.getEmail() : "")
                .setDisplayName(user.getDisplayName() != null ? user.getDisplayName() : "")
                .setSkillLevel(user.getSkillLevel() != null ? user.getSkillLevel() : "")
                .setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toEpochMilli() : 0);

        if (user.getDietaryPreferences() != null) {
            builder.addAllDietaryPreferences(user.getDietaryPreferences());
        }

        return builder.build();
    }

    public void applyUpdates(UpdateUserRequest req, User user) {
        if (req.hasEmail() && !req.getEmail().isBlank()) {
            user.setEmail(req.getEmail());
        }
        if (req.hasDisplayName() && !req.getDisplayName().isBlank()) {
            user.setDisplayName(req.getDisplayName());
        }
        if (req.getDietaryPreferencesCount() > 0) {
            user.setDietaryPreferences(new HashSet<>(req.getDietaryPreferencesList()));
        }
        if (req.hasSkillLevel() && !req.getSkillLevel().isBlank()) {
            user.setSkillLevel(req.getSkillLevel().toUpperCase(Locale.ROOT));
        }
    }

}
