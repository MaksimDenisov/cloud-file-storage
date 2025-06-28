package ru.denisovmaksim.cloudfilestorage.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ru.denisovmaksim.cloudfilestorage.exception.UserAlreadyExistException;
import ru.denisovmaksim.cloudfilestorage.model.User;
import ru.denisovmaksim.cloudfilestorage.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserService userService;

    public static final User USER = new User(1L, "ExistUser",
            new BCryptPasswordEncoder().encode("password"));

    @Test
    @DisplayName("Create correct user.")
    void createUser() {
        when(userRepository.save(Mockito.any(User.class)))
                .thenReturn(USER);
        assertEquals(USER, userService.signUp("user", "password"));
    }

    @Test
    @DisplayName("User with duplicated name should not be create.")
    void createDuplicatedUser() {
        when(userRepository.findByName(any()))
                .thenReturn(Optional.of(USER));
        String name = USER.getName();
        String password = USER.getPassword();
        assertThrows(UserAlreadyExistException.class,
                () -> userService.signUp(name, password));
    }
}
