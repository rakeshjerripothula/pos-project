package com.increff.pos.dto;

import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import com.increff.pos.model.data.FieldErrorData;
import com.increff.pos.util.ValidationUtil;
import jakarta.validation.ConstraintViolation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class AbstractDto {

    protected static <T> void checkValidList(List<T> collection) throws ApiException {
        for (T obj : collection)
            checkValid(obj);
    }

    protected static <T> void checkValid(T obj) throws ApiException {
        Set<ConstraintViolation<T>> violations = ValidationUtil.validate(obj);
        if (violations.isEmpty()) {
            return;
        }

        List<FieldErrorData> errorList = new ArrayList<>(violations.size());
        for (ConstraintViolation<T> violation : violations) {
            FieldErrorData error = new FieldErrorData();
            error.setCode("");
            error.setField(violation.getPropertyPath().toString());
            error.setMessage(violation.getMessage());
            errorList.add(error);
        }
        throw new ApiException(ApiStatus.BAD_DATA, "Input validation failed", errorList);
    }
}
