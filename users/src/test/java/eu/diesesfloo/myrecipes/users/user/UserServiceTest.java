package eu.diesesfloo.myrecipes.users.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Should find user by ID when present")
    void testGetUserById_Found() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id).username("baker").build();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(id.toString());

        assertTrue(result.isPresent());
        assertEquals("baker", result.get().getUsername());
        verify(userRepository).findById(id);
    }

    @Test
    @DisplayName("Should return empty optional when user not found")
    void testGetUserById_NotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserById(id.toString());

        assertTrue(result.isEmpty());
        verify(userRepository).findById(id);
    }

    @Test
    @DisplayName("Should save user via repository")
    void testSaveUser() {
        User user = User.builder().id(UUID.randomUUID()).username("cook").build();
        when(userRepository.save(user)).thenReturn(user);

        User saved = userService.saveUser(user);

        assertNotNull(saved);
        assertEquals("cook", saved.getUsername());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should delete user by ID")
    void testDeleteUser() {
        UUID id = UUID.randomUUID();

        userService.deleteUser(id.toString());

        verify(userRepository).deleteById(id);
    }
}
