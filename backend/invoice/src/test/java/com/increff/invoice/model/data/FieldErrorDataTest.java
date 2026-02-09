package com.increff.invoice.model.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class FieldErrorDataTest {

    @Test
    public void testDefaultConstructor() {
        FieldErrorData data = new FieldErrorData();
        
        assertNull(data.getField());
        assertNull(data.getMessage());
        assertNull(data.getCode());
    }

    @Test
    public void testParameterizedConstructor() {
        String expectedField = "fieldName";
        String expectedMessage = "error message";
        String expectedCode = "ERROR_CODE";
        
        FieldErrorData data = new FieldErrorData(expectedField, expectedMessage, expectedCode);
        
        assertEquals(expectedField, data.getField());
        assertEquals(expectedMessage, data.getMessage());
        assertEquals(expectedCode, data.getCode());
    }

    @Test
    public void testGettersAndSetters() {
        FieldErrorData data = new FieldErrorData();
        
        String expectedField = "testField";
        String expectedMessage = "test message";
        String expectedCode = "TEST_CODE";
        
        data.setField(expectedField);
        data.setMessage(expectedMessage);
        data.setCode(expectedCode);
        
        assertEquals(expectedField, data.getField());
        assertEquals(expectedMessage, data.getMessage());
        assertEquals(expectedCode, data.getCode());
    }
}
