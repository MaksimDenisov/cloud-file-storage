package ru.denisovmaksim.cloudfilestorage;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.denisovmaksim.cloudfilestorage.controller.UserController;
import ru.denisovmaksim.cloudfilestorage.exception.FileStorageException;
import ru.denisovmaksim.cloudfilestorage.exception.NotFoundException;
import ru.denisovmaksim.cloudfilestorage.exception.ObjectAlreadyExistException;
import ru.denisovmaksim.cloudfilestorage.exception.UserAlreadyExistException;

import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalControllerAdvice {

    private static final String REDIRECT_TO_ROOT = "redirect:/";

    @Value("${MAX_FILE_SIZE:10MB}")
    private String maxFileSize;

    @ExceptionHandler(UserAlreadyExistException.class)
    public String handleUserAlreadyExist(UserAlreadyExistException e, RedirectAttributes attributes) {
        attributes.addFlashAttribute("flashType", "danger");
        attributes.addFlashAttribute("flashMsg", e.getMessage());
        return "redirect:" + UserController.SIGN_UP;
    }

    @ExceptionHandler({ObjectAlreadyExistException.class, NotFoundException.class})
    public String handleExistingException(NotFoundException e, RedirectAttributes attributes) {
        setDangerMessage(e.getMessage(), attributes);
        return REDIRECT_TO_ROOT;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidatorException(MethodArgumentNotValidException e, RedirectAttributes attributes) {
        String errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(". "));
        setDangerMessage(errors, attributes);
        return "redirect:" + UserController.SIGN_UP;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public String handleDBValidationException(ConstraintViolationException e, RedirectAttributes attributes) {
        String errors = e.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(". "));
        setDangerMessage(errors, attributes);
        return REDIRECT_TO_ROOT;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException e, RedirectAttributes attributes) {
        setDangerMessage(String.format("File size exceeds %s!", maxFileSize), attributes);
        return REDIRECT_TO_ROOT;
    }

    @ExceptionHandler(FileStorageException.class)
    public String handleFileStorageException(FileStorageException e, RedirectAttributes attributes) {
        setDangerMessage("There’s a problem on our side. Please try again in a little while.", attributes);
        return REDIRECT_TO_ROOT;
    }

    @ExceptionHandler(RuntimeException.class)
    public String handleCommonException(RuntimeException e, RedirectAttributes attributes) {
        log.debug(e.getMessage());
        setDangerMessage("An error occurred. Something went wrong.", attributes);
        return REDIRECT_TO_ROOT;
    }

    private void setDangerMessage(String message, RedirectAttributes attributes) {
        attributes.addFlashAttribute("flashType", "danger");
        attributes.addFlashAttribute("flashMsg", message);
    }
}
