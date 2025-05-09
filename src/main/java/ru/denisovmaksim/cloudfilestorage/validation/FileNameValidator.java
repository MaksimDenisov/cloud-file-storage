package ru.denisovmaksim.cloudfilestorage.validation;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class FileNameValidator implements ConstraintValidator<ValidFileName, String> {
    private static final Pattern PATTERN =
            Pattern.compile("^(?!\\.)[\\p{L}0-9._\\- ]+$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && PATTERN.matcher(value).matches();
    }
}
