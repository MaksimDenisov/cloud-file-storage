package ru.denisovmaksim.cloudfilestorage.validation;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@Constraint(validatedBy = PathValidator.class)
@Documented
public @interface ValidPath {
    String ERROR_MSG_PATH_INVALID_CHARACTERS = "Not valid path";
    String message() default ERROR_MSG_PATH_INVALID_CHARACTERS;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
