package dev.baristop.portfolio.listingservice.exception;

import dev.baristop.portfolio.listingservice.response.ErrorResponse;
import dev.baristop.portfolio.listingservice.response.ValidationErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = ErrorResponse.of(HttpStatus.NOT_FOUND, ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        ValidationErrorResponse error = ValidationErrorResponse.of(
            HttpStatus.BAD_REQUEST,
            "Validation failed",
            fieldErrors
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(ValidationException ex) {
        ValidationErrorResponse error = ValidationErrorResponse.of(
            HttpStatus.BAD_REQUEST,
            ex.getMessage(),
            ex.getErrors()
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String expectedType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown type";

        String message = String.format(
            "Invalid value for '%s': expected type '%s', but got '%s'.",
            ex.getName(),
            expectedType,
            ex.getValue()
        );

        ErrorResponse error = ErrorResponse.of(HttpStatus.BAD_REQUEST, message);

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse error = ErrorResponse.of(HttpStatus.BAD_REQUEST, ex.getMessage());

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
            .collect(Collectors.toMap(
                v -> {
                    String path = v.getPropertyPath().toString();
                    // Extract parameter name from full path
                    return path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                },
                ConstraintViolation::getMessage
            ));

        ValidationErrorResponse error = ValidationErrorResponse.of(
            HttpStatus.BAD_REQUEST,
            "Validation failure",
            errors
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorResponse error = ErrorResponse.of(ex.getStatus(), ex.getMessage());

        return ResponseEntity.status(ex.getStatus()).body(error);
    }
}
