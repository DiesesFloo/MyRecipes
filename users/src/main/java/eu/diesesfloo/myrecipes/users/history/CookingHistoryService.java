package eu.diesesfloo.myrecipes.users.history;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service class for managing cooking history.
 */
@Service
@RequiredArgsConstructor
public class CookingHistoryService {

    private final CookingHistoryRepository repository;

    public CookingHistory recordCooking(UUID userId, UUID recipeId, String recipeTitle, String notes) {
        CookingHistory history = CookingHistory.builder()
                .key(CookingHistoryKey.builder()
                        .userId(userId)
                        .cookedAt(Instant.now())
                        .recipeId(recipeId)
                        .build())
                .recipeTitle(recipeTitle != null ? recipeTitle : "")
                .notes(notes != null ? notes : "")
                .build();

        return repository.save(history);
    }

    public List<CookingHistory> getHistory(UUID userId, int limit) {
        List<CookingHistory> historyList = repository.findByKeyUserId(userId);
        if (limit > 0 && historyList.size() > limit) {
            return historyList.subList(0, limit);
        }
        return historyList;
    }

}
