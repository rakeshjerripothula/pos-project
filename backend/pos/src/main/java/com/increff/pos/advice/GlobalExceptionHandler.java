package com.increff.pos.advice;

import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import com.increff.pos.exception.TsvUploadException;
import com.increff.pos.model.data.ErrorData;
import com.increff.pos.model.data.FieldErrorData;
import com.increff.pos.util.TsvErrorExportUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorData> handleApiException(ApiException e) {
        ErrorData errorData = new ErrorData(e.getStatus().name(), e.getMessage());

        if (e.hasErrors() && e.getErrors() != null) {
            for (FieldErrorData fieldError : e.getErrors()) {
                errorData.addFieldError(fieldError.getField(), fieldError.getMessage());
            }
        }

        return new ResponseEntity<>(errorData, mapStatus(e.getStatus()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorData> handleValidation(MethodArgumentNotValidException e) {
        ErrorData errorData = new ErrorData(ApiStatus.BAD_DATA.name(), "Validation failed");

        e.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errorData.addFieldError(field, message);
        });

        return new ResponseEntity<>(errorData, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorData> handleConstraintViolation(ConstraintViolationException e) {
        ErrorData errorData = new ErrorData(ApiStatus.BAD_DATA.name(), "Validation failed");

        e.getConstraintViolations().forEach(violation -> {
            String field = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errorData.addFieldError(field, message);
        });

        return new ResponseEntity<>(errorData, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorData> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        logger.error("Data integrity violation", e);
        
        String message = "Data integrity violation occurred";
        String constraintName = extractConstraintName(e.getMessage());
        
        if (constraintName != null) {
            if (constraintName.contains("uk_client_name_mrp")) {
                message = "Product with same name and MRP already exists for this client";
            } else if (constraintName.contains("uk_product_barcode")) {
                message = "Barcode already exists";
            } else {
                message = "Duplicate data detected: " + constraintName;
            }
        }
        
        return new ResponseEntity<>(
                new ErrorData(ApiStatus.CONFLICT.name(), message),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorData> handleUnknown(Exception e) {
        logger.error("Unhandled exception", e);
        return new ResponseEntity<>(
                new ErrorData(ApiStatus.INTERNAL_ERROR.name(), "Something went wrong. Please try again."),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(TsvUploadException.class)
    public ResponseEntity<byte[]> handleTsvUploadException(
            TsvUploadException e,
            HttpServletRequest request
    ) throws IOException {

        // Extract module name from request path
        String path = request.getRequestURI();
        String module = extractConstraintFromPath(path);

        byte[] errorData = TsvErrorExportUtil.exportErrorsToTsv(
                e.getErrors(),
                module
        );

        String filename = TsvErrorExportUtil.generateErrorFilename(module);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType("text/tab-separated-values")
        );
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(errorData.length);

        return new ResponseEntity<>(errorData, headers, mapStatus(e.getStatus()));
    }

    private String extractConstraintFromPath(String path) {
        if (path.contains("products")) {
            return "products";
        }
        if (path.contains("inventory")) {
            return "inventory";
        }
        return "upload";
    }

    private String extractConstraintName(String errorMessage) {
        if (errorMessage == null) {
            return null;
        }
        
        // Look for constraint name in the error message
        if (errorMessage.contains("uk_client_name_mrp")) {
            return "uk_client_name_mrp";
        }
        if (errorMessage.contains("uk_product_barcode")) {
            return "uk_product_barcode";
        }
        
        // Try to extract constraint name from "for key" pattern
        int keyIndex = errorMessage.indexOf("for key '");
        if (keyIndex != -1) {
            int start = keyIndex + 9;
            int end = errorMessage.indexOf("'", start);
            if (end != -1) {
                return errorMessage.substring(start, end);
            }
        }
        
        return null;
    }

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
