package ru.denisovmaksim.cloudfilestorage.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import ru.denisovmaksim.cloudfilestorage.dto.UserDTO;
import ru.denisovmaksim.cloudfilestorage.model.User;
import ru.denisovmaksim.cloudfilestorage.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.RequestEntity.post;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest()
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

        private static final String USER_PASS = "user";
    public static final User EXIST_USER = new User(1L,"ExistUser",
            new BCryptPasswordEncoder().encode("password"));
    public static final User NEW_USER = new User(2L, "SecondUser",
            new BCryptPasswordEncoder().encode(USER_PASS));

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRepository.save(EXIST_USER);
    }

    @Test
    @DisplayName("Create user should return user.")
    void createUser() throws Exception {
        UserDTO newUser = new UserDTO("new-user", "new_user_pass");
        mockMvc.perform(post(UserController.SIGN_UP)
                        .contentType()
                        .body())

                .andReturn()
                .getResponse();
        final User actual = userRepository.findByName(newUser.getName()).orElseThrow();
        assertEquals(newUser.getName(), actual.getName());
    }

    @Test
    @DisplayName("Create duplicated  user should return bad request.")
    void createDuplicatedUser() throws Exception {
        mockMvc.perform(post(UserController.SIGN_UP)
                .content(asJson(new UserCreationDTO(USER.getEmail(), USER.getPassword())))
                .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse();
    }
}