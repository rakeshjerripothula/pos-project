package com.increff.invoice.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

public final class ValidationUtil {

    private static final Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private ValidationUtil() {

    }

    public static <T> Set<ConstraintViolation<T>> validate(T obj) {
        return validator.validate(obj);
    }
}
