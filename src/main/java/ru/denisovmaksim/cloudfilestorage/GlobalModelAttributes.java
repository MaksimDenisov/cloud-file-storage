package ru.denisovmaksim.cloudfilestorage;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("username")
    public String populateUsername(Principal principal) {
        return principal != null ? principal.getName() : null;
    }
}
