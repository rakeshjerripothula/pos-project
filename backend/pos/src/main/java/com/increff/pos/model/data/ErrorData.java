package com.increff.pos.model.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ErrorData {

    private String code;
    private String message;
    private List<FieldErrorData> fieldErrors;

    public ErrorData(String code, String message) {
        this(code, message, new ArrayList<>());
    }

    public void addFieldError(String field, String message) {
        if (this.fieldErrors == null) {
            this.fieldErrors = new ArrayList<>();
        }
        this.fieldErrors.add(new FieldErrorData(field, message, ""));
    }
}
