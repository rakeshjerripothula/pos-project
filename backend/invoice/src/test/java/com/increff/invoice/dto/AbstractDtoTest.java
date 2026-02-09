package com.increff.invoice.dto;

import com.increff.invoice.exception.ApiException;
import com.increff.invoice.exception.ApiStatus;
import com.increff.invoice.model.data.FieldErrorData;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AbstractDtoTest {

    @Test
    public void testCheckValid_WithValidObject() {
        assertDoesNotThrow(() -> AbstractDto.checkValid("valid object"));
    }

    @Test
    public void testCheckValid_WithInvalidObject() {
        assertThrows(com.increff.invoice.exception.ApiException.class, () -> AbstractDto.checkValid(null));
    }

    @Test
    public void testCheckValidList_WithValidList() {
        assertDoesNotThrow(() -> AbstractDto.checkValidList(Collections.singletonList("valid object")));
    }

    @Test
    public void testCheckValidList_WithNullList() {
        assertThrows(NullPointerException.class, () -> AbstractDto.checkValidList(null));
    }

    @Test
    public void testCheckValidList_WithEmptyList() {
        assertDoesNotThrow(() -> AbstractDto.checkValidList(Collections.emptyList()));
    }

    @Test
    public void testCheckValidList_WithInvalidObjectInList() {
        List<String> listWithNull = new ArrayList<>();
        listWithNull.add("valid");
        listWithNull.add(null);
        
        assertThrows(com.increff.invoice.exception.ApiException.class, () -> AbstractDto.checkValidList(listWithNull));
    }

    @Test
    public void testCheckValidList_WithMixedValidObjects() {
        List<String> mixedList = List.of("valid1", "valid2", "valid3");
        
        assertDoesNotThrow(() -> AbstractDto.checkValidList(mixedList));
    }

    @Test
    public void testCheckValid_WithEmptyString() {
        assertDoesNotThrow(() -> AbstractDto.checkValid(""));
    }

    @Test
    public void testCheckValid_WithValidNumber() {
        assertDoesNotThrow(() -> AbstractDto.checkValid(42));
    }

    @Test
    public void testCheckValid_WithValidBoolean() {
        assertDoesNotThrow(() -> AbstractDto.checkValid(true));
    }

    @Test
    public void testCheckValidApiException_WithFieldErrors() {
        try {
            AbstractDto.checkValid(new TestInvalidObject());
            fail("Expected ApiException");
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Input validation failed", e.getMessage());
            assertNotNull(e.getErrors());
            assertFalse(e.getErrors().isEmpty());
        }
    }

    // Test helper class for validation testing
    private static class TestInvalidObject {
        @jakarta.validation.constraints.NotNull(message = "Name cannot be null")
        private String name = null;
        
        @jakarta.validation.constraints.Size(min = 5, max = 10, message = "Description must be between 5 and 10 characters")
        private String description = "abc";
    }
}
