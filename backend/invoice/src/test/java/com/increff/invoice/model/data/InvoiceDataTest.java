package com.increff.invoice.model.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class InvoiceDataTest {

    @Test
    public void testGettersAndSetters() {
        InvoiceData data = new InvoiceData();
        
        assertNull(data.getBase64Pdf());
        
        String expectedBase64 = "base64pdfcontent";
        data.setBase64Pdf(expectedBase64);
        
        assertEquals(expectedBase64, data.getBase64Pdf());
    }
}
