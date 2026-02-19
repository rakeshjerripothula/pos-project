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
        
        assertNull(form.getOrderId());
        assertNull(form.getClientName());
        assertNull(form.getItems());
        assertNull(form.getTotalAmount());
        
        Integer expectedOrderId = 1;
        String expectedClientName = "Test Client";
        InvoiceItemForm expectedItem = new InvoiceItemForm();
        expectedItem.setProductName("Test Product");
        expectedItem.setQuantity(1);
        expectedItem.setSellingPrice(new BigDecimal("100.00"));
        expectedItem.setLineTotal(new BigDecimal("100.00"));
        BigDecimal expectedTotalAmount = new BigDecimal("100.00");
        
        form.setOrderId(expectedOrderId);
        form.setClientName(expectedClientName);
        form.setItems(Collections.singletonList(expectedItem));
        form.setTotalAmount(expectedTotalAmount);
        
        assertEquals(expectedOrderId, form.getOrderId());
        assertEquals(expectedClientName, form.getClientName());
        assertEquals(1, form.getItems().size());
        assertEquals(expectedItem, form.getItems().get(0));
        assertEquals(expectedTotalAmount, form.getTotalAmount());
    }
}
