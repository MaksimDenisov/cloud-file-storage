package ru.denisovmaksim.cloudfilestorage.validation;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathValidator implements ConstraintValidator<ValidPath, String> {
    private Pattern pattern;

    @Override
    public void initialize(ValidPath constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        String regexp = "^([^\\\\:*?\"<>|]*\\/)?$";
        pattern = Pattern.compile(regexp);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }
}
