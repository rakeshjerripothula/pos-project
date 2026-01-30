package com.increff.pos.model.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FieldErrorData {

    private String field;
    private String message;
    private String code;
}
