package com.increff.pos.exception;

import com.increff.pos.model.data.TsvUploadError;
import lombok.Getter;

import java.util.List;

@Getter
public class TsvUploadException extends RuntimeException {

    private final List<TsvUploadError> errors;
    private final ApiStatus status;

    public TsvUploadException(List<TsvUploadError> errors, ApiStatus status) {
        super("TSV upload failed");
        this.errors = errors;
        this.status = status;
    }

}
