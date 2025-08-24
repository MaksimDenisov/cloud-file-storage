package ru.denisovmaksim.cloudfilestorage;

import jakarta.servlet.http.HttpServletRequest;
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
import ru.denisovmaksim.cloudfilestorage.exception.ImageProcessingException;
import ru.denisovmaksim.cloudfilestorage.exception.NotFoundException;
import ru.denisovmaksim.cloudfilestorage.exception.ObjectAlreadyExistException;
import ru.denisovmaksim.cloudfilestorage.exception.RootFolderModificationException;
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
        log.error("User already exist: {}", e.getMessage());
        attributes.addFlashAttribute("flashType", "danger");
        attributes.addFlashAttribute("flashMsg", "User already exist");
        return "redirect:" + UserController.SIGN_UP;
    }

    @ExceptionHandler(ObjectAlreadyExistException.class)
    public String handleExistingException(Exception e, HttpServletRequest request, RedirectAttributes attributes) {
        log.error("Object already exist: {}", e.getMessage());
        String path = request.getParameter("path");
        if (path != null && !path.isEmpty()) {
            attributes.addAttribute("path", path);
        }
        setDangerMessage("Object already exist", attributes);
        return REDIRECT_TO_ROOT;
    }

    @ExceptionHandler(NotFoundException.class)
    public String handleNotFoundException(Exception e, HttpServletRequest request, RedirectAttributes attributes) {
        log.error("Object not exist: {}", e.getMessage());
        String path = request.getParameter("path");
        if (path != null && !path.isEmpty()) {
            attributes.addAttribute("path", path);
        }
        setDangerMessage("Object already exist", attributes);
        return REDIRECT_TO_ROOT;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidatorException(MethodArgumentNotValidException e, RedirectAttributes attributes) {
        log.error("Method Argument Not Valid: {}", e.getMessage(), e);
        String errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(". "));
        setDangerMessage(errors, attributes);
        return "redirect:" + UserController.SIGN_UP;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public String handleDBValidationException(ConstraintViolationException e,
                                              HttpServletRequest request, RedirectAttributes attributes) {
        log.error("Validation failed: {}", e.getConstraintViolations(), e);
        String errors = e.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(". "));
        String path = request.getParameter("path");
        if (path != null && !path.isEmpty()) {
            attributes.addAttribute("path", path);
        }
        setDangerMessage(errors, attributes);
        return REDIRECT_TO_ROOT;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException e, RedirectAttributes attributes) {
        log.error("File size exceeds {} : {}", maxFileSize, e.getMessage());
        setDangerMessage(String.format("File size exceeds %s!", maxFileSize), attributes);
        return REDIRECT_TO_ROOT;
    }

    @ExceptionHandler(FileStorageException.class)
    public String handleFileStorageException(FileStorageException e, RedirectAttributes attributes) {
        log.error("File storage exception: {}", e.getMessage());
        setDangerMessage("There’s a problem on our side. Please try again in a little while.", attributes);
        return REDIRECT_TO_ROOT;
    }

    @ExceptionHandler(RootFolderModificationException.class)
    public String handleUserAlreadyExist(RootFolderModificationException e, RedirectAttributes attributes) {
        log.error("Root folder modification: {}", e.getMessage());
        attributes.addFlashAttribute("flashType", "danger");
        attributes.addFlashAttribute("flashMsg", "Not success");
        return "redirect:" + UserController.SIGN_UP;
    }

    @ExceptionHandler(ImageProcessingException.class)
    public String handleImageProcessingException(ImageProcessingException e, RedirectAttributes attributes) {
        log.error("Image Processing Exception", e);
        setDangerMessage("This file can’t be previewed. Please download it to open.", attributes);
        return REDIRECT_TO_ROOT;
    }

    @ExceptionHandler(Exception.class)
    public String handleCommonException(Exception e, RedirectAttributes attributes) {
        log.error("Unexpected error occurred", e);
        setDangerMessage("An error occurred. Something went wrong.", attributes);
        return REDIRECT_TO_ROOT;
    }

    private void setDangerMessage(String message, RedirectAttributes attributes) {
        attributes.addFlashAttribute("flashType", "danger");
        attributes.addFlashAttribute("flashMsg", message);
    }
}
