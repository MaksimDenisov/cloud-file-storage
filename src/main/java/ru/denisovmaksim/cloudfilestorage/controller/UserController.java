package ru.denisovmaksim.cloudfilestorage.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.denisovmaksim.cloudfilestorage.dto.UserDTO;
import ru.denisovmaksim.cloudfilestorage.service.UserService;

import org.springframework.security.authentication.AuthenticationManager;

@Controller()
@Slf4j
public class UserController {
    public static final String SIGN_IN = "/sign-in";
    public static final String SIGN_UP = "/sign-up";

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    private final SecurityContextRepository securityContextRepository;

    public UserController(UserService userService,
                          AuthenticationManager authenticationManager,
                          SecurityContextRepository securityContextRepository) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @GetMapping(SIGN_IN)
    public String getSignInPage(RedirectAttributes attributes, @RequestParam(required = false) String error) {
        log.info("GET: Sign in ");
        if (error != null) {
            attributes.addFlashAttribute("flashType", "danger");
            attributes.addFlashAttribute("flashMsg", "Incorrect login or password.");
            return "redirect:sign-in";
        }
        return "sign-in";

    }

    @GetMapping(SIGN_UP)
    public String getSignUpPage() {
        log.info("GET: Sign up");
        return "sign-up";
    }

    @PostMapping(SIGN_UP)
    public String signUp(@ModelAttribute @Valid UserDTO user,
                         RedirectAttributes attributes,
                         HttpServletRequest request,
                         HttpServletResponse response) {
        log.info("POST: Sign up");
        userService.signUp(user.getName(), user.getPassword());
        authenticateUserAndSetSession(user, request, response);
        attributes.addFlashAttribute("flashType", "success");
        attributes.addFlashAttribute("flashMsg",
                String.format("Congratulation, %s! Now you can use cloud file storage.", user.getName()));
        return "redirect:/";
    }

    private void authenticateUserAndSetSession(UserDTO user,
                                               HttpServletRequest request, HttpServletResponse response) {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(user.getName(), user.getPassword());
        Authentication authentication = authenticationManager.authenticate(token);
        SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder
                .getContextHolderStrategy();

        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(context);

        securityContextRepository.saveContext(context, request, response);
    }
}

