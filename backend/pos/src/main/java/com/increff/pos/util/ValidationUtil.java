package com.increff.pos.util;

import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
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

    public static void validateOptionalDateRange(ZonedDateTime startDate, ZonedDateTime endDate) {
        if (Objects.nonNull(startDate) && Objects.nonNull(endDate)) {
            if (startDate.isAfter(endDate)) {
                throw new ApiException(
                    ApiStatus.BAD_DATA,
                    "Start date cannot be after end date",
                    "dates",
                    "Start date cannot be after end date"
                );
            }
        }
    }

    public static void validateOptionalDateRange(LocalDate startDate, LocalDate endDate) {
        if (Objects.nonNull(startDate) && Objects.nonNull(endDate)) {
            if (startDate.isAfter(endDate)) {
                throw new ApiException(
                    ApiStatus.BAD_DATA,
                    "Start date cannot be after end date",
                    "dates",
                    "Start date cannot be after end date"
                );
            }
        }
    }
}
