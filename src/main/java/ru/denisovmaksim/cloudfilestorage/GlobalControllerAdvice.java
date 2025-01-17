package ru.denisovmaksim.cloudfilestorage;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.denisovmaksim.cloudfilestorage.controller.UserController;
import ru.denisovmaksim.cloudfilestorage.exceptions.StorageObjectNotFoundException;
import ru.denisovmaksim.cloudfilestorage.exceptions.UserAlreadyExistException;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Value("${MAX_FILE_SIZE:10MB}")
    private String maxFileSize;

    @ExceptionHandler(UserAlreadyExistException.class)
    public String handleUserAlreadyExist(UserAlreadyExistException e, RedirectAttributes attributes) {
        attributes.addFlashAttribute("flashType", "danger");
        attributes.addFlashAttribute("flashMsg", e.getMessage());
        return "redirect:" + UserController.SIGN_UP;
    }

    @ExceptionHandler(StorageObjectNotFoundException.class)
    public ModelAndView handleNotFoundException(StorageObjectNotFoundException e, Authentication authentication) {
        //   view.addObject("path", e.getPath());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("path", e.getPath());
        modelAndView.addObject("username", authentication.getName());
        modelAndView.setViewName("not-found");
        return modelAndView;
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
        String errors = e.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(". "));
        attributes.addFlashAttribute("flashType", "danger");
        attributes.addFlashAttribute("flashMsg", errors);
        return "redirect:/";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException e, RedirectAttributes attributes) {
        attributes.addFlashAttribute("flashType", "danger");
        attributes.addFlashAttribute("flashMsg", String.format("File size exceeds %s!", maxFileSize));
        return "redirect:/";
    }
}
