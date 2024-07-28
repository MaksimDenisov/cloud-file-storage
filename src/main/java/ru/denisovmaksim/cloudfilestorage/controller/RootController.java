package ru.denisovmaksim.cloudfilestorage.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;


@Controller
@Slf4j
public class RootController {
    @GetMapping("/")
    public String welcome(Model model, Authentication authentication) {
        if (authentication != null) {
            model.addAttribute("username", authentication.getName());
        }
        model.addAttribute("flash", "welcome");
        log.info("Welcome " + Optional.ofNullable(model.getAttribute("username"))
                .orElse(" anonymous"));
        return "folders";
    }

}
