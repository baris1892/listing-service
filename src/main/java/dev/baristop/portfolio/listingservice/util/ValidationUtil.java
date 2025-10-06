package dev.baristop.portfolio.listingservice.util;

import dev.baristop.portfolio.listingservice.exception.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ValidationUtil {
    private final Validator validator;

    public ValidationUtil(Validator validator) {
        this.validator = validator;
    }

    public <T> void validate(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            Map<String, String> errors = violations.stream()
                .collect(Collectors.toMap(
                    v -> v.getPropertyPath().toString(),
                    ConstraintViolation::getMessage
                ));
            throw new ValidationException(errors);
        }
    }
}
