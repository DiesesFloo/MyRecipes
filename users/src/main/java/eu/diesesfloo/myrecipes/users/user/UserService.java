package eu.diesesfloo.myrecipes.users.user;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Service class for managing users with Redis caching.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Cacheable(value = "users_by_id", key = "#id", unless = "#result.empty()")
    public Optional<User> getUserById(String id) {
        return userRepository.findById(UUID.fromString(id));
    }

    @CacheEvict(value = "users_by_id", key = "#user.id.toString()")
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @CacheEvict(value = "users_by_id", key = "#id")
    public void deleteUser(String id) {
        userRepository.deleteById(UUID.fromString(id));
    }

}
