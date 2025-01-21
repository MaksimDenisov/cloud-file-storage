package ru.denisovmaksim.cloudfilestorage.validation;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FolderNameValidator implements ConstraintValidator<ValidFolderName, String> {
    private Pattern pattern;

    @Override
    public void initialize(ValidFolderName constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        String regexp = "^[^/\\\\:.*?\"<>|]+$";
        pattern = Pattern.compile(regexp);
    }
    public boolean isValid(String value, ConstraintValidatorContext context) {
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }
}
