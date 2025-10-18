package ru.denisovmaksim.cloudfilestorage.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import ru.denisovmaksim.cloudfilestorage.util.PathUtil;

public class PathValidator implements ConstraintValidator<ValidPath, String> {

    private PathType pathType;

    PathValidator(@Autowired(required = false) PathType pathType) {
        this.pathType = (pathType != null) ? pathType : PathType.FILEPATH;
    }

    @Override
    public void initialize(ValidPath constraintAnnotation) {
        pathType = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        boolean isValid = switch (pathType) {
            case DIR -> PathUtil.isValid(value) && PathUtil.isDir(value);
            case FILEPATH -> PathUtil.isValid(value) && !PathUtil.isDir(value);
            case NAME -> PathUtil.isValid(value) && PathUtil.getBaseName(value).equals(value);
        };

        if (!isValid && context != null) {
            context.disableDefaultConstraintViolation();
            String errorMessage = switch (pathType) {
                case DIR -> "incorrect dir path";
                case FILEPATH -> "incorrect file path";
                case NAME -> "incorrect file name";
            };
            errorMessage = String.format("%s is %s", value, errorMessage);
            context.buildConstraintViolationWithTemplate(errorMessage)
                    .addConstraintViolation();
        }
        return isValid;
    }
}
