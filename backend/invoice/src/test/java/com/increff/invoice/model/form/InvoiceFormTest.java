package com.increff.invoice.model.form;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class InvoiceFormTest {

    @Test
    public void testGettersAndSetters() {
        InvoiceForm form = new InvoiceForm();
        
        assertNull(form.getInvoiceNumber());
        assertNull(form.getInvoiceDate());
        assertNull(form.getClientName());
        assertNull(form.getItems());
        assertNull(form.getTotalAmount());
        
        String expectedInvoiceNumber = "INV-001";
        ZonedDateTime expectedInvoiceDate = ZonedDateTime.now();
        String expectedClientName = "Test Client";
        InvoiceItemForm expectedItem = new InvoiceItemForm();
        expectedItem.setProductName("Test Product");
        expectedItem.setQuantity(1);
        expectedItem.setSellingPrice(new BigDecimal("100.00"));
        expectedItem.setLineTotal(new BigDecimal("100.00"));
        BigDecimal expectedTotalAmount = new BigDecimal("100.00");
        
        form.setInvoiceNumber(expectedInvoiceNumber);
        form.setInvoiceDate(expectedInvoiceDate);
        form.setClientName(expectedClientName);
        form.setItems(Collections.singletonList(expectedItem));
        form.setTotalAmount(expectedTotalAmount);
        
        assertEquals(expectedInvoiceNumber, form.getInvoiceNumber());
        assertEquals(expectedInvoiceDate, form.getInvoiceDate());
        assertEquals(expectedClientName, form.getClientName());
        assertEquals(1, form.getItems().size());
        assertEquals(expectedItem, form.getItems().get(0));
        assertEquals(expectedTotalAmount, form.getTotalAmount());
    }
}
