package ru.denisovmaksim.cloudfilestorage;

import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.denisovmaksim.cloudfilestorage.controller.UserController;
import ru.denisovmaksim.cloudfilestorage.model.User;
import ru.denisovmaksim.cloudfilestorage.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@EnableRedisHttpSession
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Container
    private static MySQLContainer mySQLContainer = new MySQLContainer("mysql:8:0:26")
            .withDatabaseName("db_name")
            .withUsername("root")
            .withPassword("password");
    @Container
    private static final RedisContainer REDIS_CONTAINER =
            new RedisContainer(DockerImageName.parse("redis:latest"))
                    .withExposedPorts(6379)
                    .withReuse(true);
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySQLContainer::getUsername);
        registry.add("spring.datasource.password", mySQLContainer::getPassword);

        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379).toString());
    }

    private static final String USER_PASS = "user-password";
    public static final User EXIST_USER = new User(1L, "ExistUser",
            new BCryptPasswordEncoder().encode("password"));
    public static final User NEW_USER = new User(2L, "NewUser",
            new BCryptPasswordEncoder().encode(USER_PASS));

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRepository.save(EXIST_USER);
    }

    @Test
    @DisplayName("Create correct user.")
    void createUser() throws Exception {
        mockMvc.perform(post(UserController.SIGN_UP)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", NEW_USER.getName())
                        .param("password", USER_PASS))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
        assertTrue(userRepository.findByName(NEW_USER.getName()).isPresent());
    }

    @Test
    @DisplayName("User with duplicated name should not be create.")
    void createDuplicatedUser() throws Exception {
        mockMvc.perform(post(UserController.SIGN_UP)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", EXIST_USER.getName())
                        .param("password", "PASSWORD"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(UserController.SIGN_UP))
                .andExpect(flash().attributeExists("flashType", "flashMsg"))
                .andReturn();
    }

    @Test
    @DisplayName("User with name shorter 3 character should not be create.")
    void createIncorrectNameUser() throws Exception {
        mockMvc.perform(post(UserController.SIGN_UP)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "AA")
                        .param("password", "PASSWORD"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(UserController.SIGN_UP))
                .andExpect(flash().attributeExists("flashType", "flashMsg"))
                .andReturn();
    }

    @Test
    @DisplayName("User with password shorter 6 character should not be create.")
    void createIncorrectPasswordUser() throws Exception {
        mockMvc.perform(post(UserController.SIGN_UP)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Name")
                        .param("password", "123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(UserController.SIGN_UP))
                .andExpect(flash().attributeExists("flashType", "flashMsg"))
                .andReturn();
    }
}
