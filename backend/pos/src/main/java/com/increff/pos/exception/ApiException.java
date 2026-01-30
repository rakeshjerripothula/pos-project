package com.increff.pos.exception;

import com.increff.pos.model.data.FieldErrorData;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class ApiException extends RuntimeException {

    private final ApiStatus status;
    private final List<FieldErrorData> errors;

    public ApiException(ApiStatus status, String message) {
        super(message);
        this.status = status;
        this.errors = Collections.emptyList();
    }

    public ApiException(ApiStatus status, String message, List<FieldErrorData> errors) {
        super(message);
        this.status = status;
        this.errors = errors == null ? Collections.emptyList() : errors;
    }

    public ApiException(ApiStatus status, String message, String field, String errorMessage) {
        super(message);
        this.status = status;

        FieldErrorData error = new FieldErrorData();
        error.setCode("");
        error.setField(field);
        error.setMessage(errorMessage);

        this.errors = List.of(error);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
