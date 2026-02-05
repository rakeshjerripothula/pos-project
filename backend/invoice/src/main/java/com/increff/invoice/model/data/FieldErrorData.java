package com.increff.invoice.model.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FieldErrorData {

    private String field;
    private String message;
    private String code;

    public FieldErrorData() {
    }

    public FieldErrorData(String field, String message, String code){
        this.field = field;
        this.message = message;
        this.code = code;
    }

}
