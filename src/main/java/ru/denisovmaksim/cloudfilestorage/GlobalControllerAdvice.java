package ru.denisovmaksim.cloudfilestorage;

import jakarta.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.denisovmaksim.cloudfilestorage.controller.UserController;
import ru.denisovmaksim.cloudfilestorage.exceptions.UserAlreadyExistException;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalControllerAdvice {
    @ExceptionHandler(UserAlreadyExistException.class)
    public String handleUserAlreadyExist(UserAlreadyExistException e, RedirectAttributes attributes) {
        attributes.addFlashAttribute("flashType", "danger");
        attributes.addFlashAttribute("flashMsg", e.getMessage());
        return "redirect:" + UserController.SIGN_UP;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidatorException(MethodArgumentNotValidException e, RedirectAttributes attributes) {
        String errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(". "));
        attributes.addFlashAttribute("flashType", "danger");
        attributes.addFlashAttribute("flashMsg", errors);
        return "redirect:" + UserController.SIGN_UP;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public String handleDBValidationException(ConstraintViolationException e, RedirectAttributes attributes) {
        attributes.addFlashAttribute("flashType", "danger");
        attributes.addFlashAttribute("flashMsg", "Error");
        return "redirect:/";
    }
}
