package com.increff.pos.util;

import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest {

    @Test
    void should_validate_valid_object() {
        // Arrange
        TestValidObject validObject = new TestValidObject();
        validObject.setName("Test Name");
        validObject.setEmail("test@example.com");
        validObject.setAge(25);
        
        // Act
        Set<ConstraintViolation<TestValidObject>> violations = ValidationUtil.validate(validObject);
        
        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void should_return_violations_when_invalid_object() {
        // Arrange
        TestValidObject invalidObject = new TestValidObject();
        invalidObject.setName(""); // Invalid: blank
        invalidObject.setEmail("invalid-email"); // Invalid: not email format
        invalidObject.setAge(null); // Invalid: null
        
        // Act
        Set<ConstraintViolation<TestValidObject>> violations = ValidationUtil.validate(invalidObject);
        
        // Assert
        assertEquals(4, violations.size());
        
        violations.forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            if ("name".equals(propertyPath)) {
                assertTrue(violation.getMessage().contains("must not be blank") || 
                          violation.getMessage().contains("size must be between"));
            } else if ("email".equals(propertyPath)) {
                assertTrue(violation.getMessage().contains("must be a well-formed email") || 
                          violation.getMessage().contains("must not be blank"));
            } else if ("age".equals(propertyPath)) {
                assertTrue(violation.getMessage().contains("must not be null"));
            }
        });
    }

    @Test
    void validate_nullObject_throwsApiException() {
        // Act
        ApiException exception = assertThrows(
                ApiException.class,
                () -> ValidationUtil.validate(null)
        );

        // Assert
        assertEquals(ApiStatus.BAD_DATA, exception.getStatus());
        assertEquals("Input cannot be null", exception.getMessage());
    }


    @Test
    void should_validate_object_with_partial_violations() {
        // Arrange
        TestValidObject partiallyInvalidObject = new TestValidObject();
        partiallyInvalidObject.setName("Valid Name");
        partiallyInvalidObject.setEmail("invalid-email"); // Invalid: not email format
        partiallyInvalidObject.setAge(30);
        
        // Act
        Set<ConstraintViolation<TestValidObject>> violations = ValidationUtil.validate(partiallyInvalidObject);
        
        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<TestValidObject> violation = violations.iterator().next();
        assertEquals("email", violation.getPropertyPath().toString());
        assertTrue(violation.getMessage().contains("must be a well-formed email"));
    }

    @Test
    void should_validate_object_with_size_constraints() {
        // Arrange
        TestValidObject objectWithInvalidSize = new TestValidObject();
        objectWithInvalidSize.setName("A"); // Invalid: size too small
        objectWithInvalidSize.setEmail("test@example.com");
        objectWithInvalidSize.setAge(25);
        
        // Act
        Set<ConstraintViolation<TestValidObject>> violations = ValidationUtil.validate(objectWithInvalidSize);
        
        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<TestValidObject> violation = violations.iterator().next();
        assertEquals("name", violation.getPropertyPath().toString());
        assertTrue(violation.getMessage().contains("size must be between"));
    }

    @Test
    void should_validate_object_with_multiple_violations_same_field() {
        // Arrange
        TestValidObject objectWithMultipleIssues = new TestValidObject();
        objectWithMultipleIssues.setName(""); // Invalid: blank and size too small
        objectWithMultipleIssues.setEmail("test@example.com");
        objectWithMultipleIssues.setAge(25);
        
        // Act
        Set<ConstraintViolation<TestValidObject>> violations = ValidationUtil.validate(objectWithMultipleIssues);
        
        // Assert
        assertEquals(2, violations.size());
        
        violations.forEach(violation -> {
            assertEquals("name", violation.getPropertyPath().toString());
            String message = violation.getMessage();
            assertTrue(message.contains("must not be blank") || message.contains("size must be between"));
        });
    }

    // Test class for validation
    private static class TestValidObject {
        @NotBlank(message = "Name must not be blank")
        @Size(min = 2, max = 50, message = "Name size must be between 2 and 50")
        private String name;

        @Email(message = "Email must be a well-formed email")
        @NotBlank(message = "Email must not be blank")
        private String email;

        @NotNull(message = "Age must not be null")
        private Integer age;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
    }
}
