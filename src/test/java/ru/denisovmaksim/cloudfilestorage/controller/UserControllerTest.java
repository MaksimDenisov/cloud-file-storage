package ru.denisovmaksim.cloudfilestorage.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import ru.denisovmaksim.cloudfilestorage.GlobalControllerAdvice;
import ru.denisovmaksim.cloudfilestorage.model.User;
import ru.denisovmaksim.cloudfilestorage.service.AuthenticationService;
import ru.denisovmaksim.cloudfilestorage.service.UserService;
import ru.denisovmaksim.cloudfilestorage.exception.UserAlreadyExistException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalControllerAdvice.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationService authenticationService;

    private static final String USER_PASS = "user-password";
    public static final User EXIST_USER = new User(1L, "ExistUser",
            new BCryptPasswordEncoder().encode("password"));
    public static final User NEW_USER = new User(2L, "NewUser",
            new BCryptPasswordEncoder().encode(USER_PASS));

    @Test
    @WithAnonymousUser
    @DisplayName("Create correct user.")
    void createUser() throws Exception {
        when(userService.signUp(any(), any())).thenReturn(NEW_USER);
        mockMvc.perform(post(UserController.SIGN_UP).with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "name")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("User with duplicated name should not be create.")
    void createDuplicatedUser() throws Exception {
        doThrow(new UserAlreadyExistException("User Already Exist"))
                .when(userService).signUp(anyString(), anyString());

        mockMvc.perform(post(UserController.SIGN_UP)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", EXIST_USER.getName())
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sign-up"));
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
