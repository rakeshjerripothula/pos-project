package com.increff.invoice.util;

import com.increff.invoice.exception.ApiException;
import com.increff.invoice.exception.ApiStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Objects;
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
        if (obj == null) {
            throw new ApiException(
                    ApiStatus.BAD_DATA,
                    "Input cannot be null"
            );
        }
        return validator.validate(obj);
    }

}
