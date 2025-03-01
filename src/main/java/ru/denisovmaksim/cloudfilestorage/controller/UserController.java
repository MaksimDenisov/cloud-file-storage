package ru.denisovmaksim.cloudfilestorage.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.denisovmaksim.cloudfilestorage.dto.UserDTO;
import ru.denisovmaksim.cloudfilestorage.service.AuthenticationService;
import ru.denisovmaksim.cloudfilestorage.service.UserService;

@Controller()
@Slf4j
@AllArgsConstructor
public class UserController {
    public static final String SIGN_IN = "/sign-in";
    public static final String SIGN_UP = "/sign-up";

    private final UserService userService;

    private final AuthenticationService authenticationService;

    @GetMapping(SIGN_IN)
    public String getSignInPage(RedirectAttributes attributes, @RequestParam(required = false) String error) {
        log.info("GET: Sign in ");
        if (error != null) {
            attributes.addFlashAttribute("flashType", "danger");
            attributes.addFlashAttribute("flashMsg", "Incorrect login or password.");
            return "redirect:sign-in";
        }
        return "auth/sign-in";

    }

    @GetMapping(SIGN_UP)
    public String getSignUpPage() {
        log.info("GET: Sign up");
        return "auth/sign-up";
    }

    @PostMapping(SIGN_UP)
    public String signUp(@ModelAttribute @Valid UserDTO user,
                         RedirectAttributes attributes,
                         HttpServletRequest request,
                         HttpServletResponse response) {
        log.info("POST: Sign up");
        userService.signUp(user.getName(), user.getPassword());
        authenticationService.authenticateUserAndSetSession(user, request, response);
        attributes.addFlashAttribute("flashType", "success");
        attributes.addFlashAttribute("flashMsg",
                String.format("Congratulation, %s! Now you can use cloud file storage.", user.getName()));
        return "redirect:/";
    }


}

