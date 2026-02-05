package com.increff.pos.advice;

import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import com.increff.pos.model.data.ErrorData;
import com.increff.pos.model.data.FieldErrorData;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // -------- Business exceptions --------
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorData> handleApiException(ApiException e) {
        ErrorData errorData = new ErrorData(e.getStatus().name(), e.getMessage());

        // Include field-level errors if present
        if (e.hasErrors() && e.getErrors() != null) {
            for (FieldErrorData fieldError : e.getErrors()) {
                errorData.addFieldError(fieldError.getField(), fieldError.getMessage());
            }
        }

        return new ResponseEntity<>(errorData, mapStatus(e.getStatus()));
    }

    // -------- Form validation (@Valid) --------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorData> handleValidation(MethodArgumentNotValidException e) {
        ErrorData errorData = new ErrorData(
                ApiStatus.BAD_DATA.name(),
                "Validation failed"
        );

        e.getBindingResult().getAllErrors().forEach(error -> {
            String field = error.getObjectName();
            String message = error.getDefaultMessage();
            errorData.addFieldError(field, message);
        });

        return new ResponseEntity<>(errorData, HttpStatus.BAD_REQUEST);
    }

    // -------- @Validated param validation --------
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorData> handleConstraintViolation(ConstraintViolationException e) {
        ErrorData errorData = new ErrorData(
                ApiStatus.BAD_DATA.name(),
                "Validation failed"
        );

        e.getConstraintViolations().forEach(violation -> {
            String field = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errorData.addFieldError(field, message);
        });

        return new ResponseEntity<>(errorData, HttpStatus.BAD_REQUEST);
    }

    // -------- Fallback (never leak stacktrace) --------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorData> handleUnknown(Exception e) {
        logger.error("Unhandled exception", e);
        return new ResponseEntity<>(
                new ErrorData(
                        ApiStatus.INTERNAL_ERROR.name(),
                        "Something went wrong. Please try again."
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    // -------- ApiStatus → HTTP mapping --------
    private HttpStatus mapStatus(ApiStatus status) {
        return switch (status) {
            case BAD_DATA, BAD_REQUEST -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
