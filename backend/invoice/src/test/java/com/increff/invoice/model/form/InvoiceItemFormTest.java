package com.increff.invoice.model.form;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class InvoiceItemFormTest {

    @Test
    public void testGettersAndSetters() {
        InvoiceItemForm form = new InvoiceItemForm();
        
        assertNull(form.getProductName());
        assertNull(form.getQuantity());
        assertNull(form.getSellingPrice());
        assertNull(form.getLineTotal());
        
        String expectedProductName = "Test Product";
        Integer expectedQuantity = 5;
        BigDecimal expectedSellingPrice = new BigDecimal("99.99");
        BigDecimal expectedLineTotal = new BigDecimal("499.95");
        
        form.setProductName(expectedProductName);
        form.setQuantity(expectedQuantity);
        form.setSellingPrice(expectedSellingPrice);
        form.setLineTotal(expectedLineTotal);
        
        assertEquals(expectedProductName, form.getProductName());
        assertEquals(expectedQuantity, form.getQuantity());
        assertEquals(expectedSellingPrice, form.getSellingPrice());
        assertEquals(expectedLineTotal, form.getLineTotal());
    }
}
