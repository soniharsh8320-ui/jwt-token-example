package spring.security.jwt.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import spring.security.jwt.entity.User;
import spring.security.jwt.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CONFLICT;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void registerUserShouldThrowConflictWhenUserAlreadyExists() {
        User request = new User(null, "john", "john123");
        when(userRepository.existsByUsername("john")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.registerUser(request)
        );

        assertEquals(CONFLICT.value(), exception.getStatusCode().value());
        assertEquals("User already exists", exception.getReason());
        verify(userRepository).existsByUsername("john");
    }

    @Test
    void registerUserShouldSaveEncodedPasswordWhenUserDoesNotExist() {
        User request = new User(null, "john", "john123");
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(passwordEncoder.encode("john123")).thenReturn("encoded-password");

        userService.registerUser(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("john", userCaptor.getValue().getUsername());
        assertEquals("encoded-password", userCaptor.getValue().getPassword());
    }
}
