package com.increff.invoice.dto;

import com.increff.invoice.api.InvoiceApi;
import com.increff.invoice.exception.ApiException;
import com.increff.invoice.model.data.InvoiceData;
import com.increff.invoice.model.form.InvoiceForm;
import com.increff.invoice.model.form.InvoiceItemForm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InvoiceDtoTest {

    @Mock
    private InvoiceApi invoiceApi;

    @InjectMocks
    private InvoiceDto invoiceDto;

    @Test
    public void testGenerate() {
        InvoiceForm form = new InvoiceForm();
        form.setOrderId(1);
        form.setClientName("Test Client");
        form.setItems(java.util.Collections.emptyList());
        form.setTotalAmount(BigDecimal.ZERO);

        String expectedBase64 = "base64pdfcontent";

        // Stub ONLY the API call
        when(invoiceApi.generateInvoice(any())).thenReturn(expectedBase64);

        InvoiceData result = invoiceDto.generate(form);

        assertEquals(expectedBase64, result.getBase64Pdf());
    }

    @Test
    public void testGenerateWithNullForm() {
        assertThrows(com.increff.invoice.exception.ApiException.class, () -> invoiceDto.generate(null));
    }

    @Test
    public void testGenerateWithInvalidForm() {
        InvoiceForm form = new InvoiceForm();

        assertThrows(ApiException.class, () -> invoiceDto.generate(form));
    }

    @Test
    public void testGenerateWithValidFormWithItems() {
        InvoiceItemForm item = new InvoiceItemForm();
        item.setProductName("Test Product");
        item.setQuantity(1);
        item.setSellingPrice(new BigDecimal("100.00"));
        item.setLineTotal(new BigDecimal("100.00"));

        InvoiceForm form = new InvoiceForm();
        form.setOrderId(2);
        form.setClientName("Test Client");
        form.setItems(Collections.singletonList(item));
        form.setTotalAmount(new BigDecimal("100.00"));

        String expectedBase64 = "base64pdfcontentwithitems";

        when(invoiceApi.generateInvoice(any())).thenReturn(expectedBase64);

        InvoiceData result = invoiceDto.generate(form);

        assertEquals(expectedBase64, result.getBase64Pdf());
    }

    @Test
    public void testGenerateWithEmptyItemsList() {
        InvoiceForm form = new InvoiceForm();
        form.setOrderId(3);
        form.setClientName("Test Client");
        form.setItems(Collections.emptyList());
        form.setTotalAmount(BigDecimal.ZERO);

        String expectedBase64 = "base64pdfemptyitems";

        when(invoiceApi.generateInvoice(any())).thenReturn(expectedBase64);

        InvoiceData result = invoiceDto.generate(form);

        assertEquals(expectedBase64, result.getBase64Pdf());
    }

    @Test
    public void testGenerateWithApiException() {
        InvoiceForm form = new InvoiceForm();
        form.setOrderId(4);
        form.setClientName("Test Client");
        form.setItems(Collections.emptyList());
        form.setTotalAmount(BigDecimal.ZERO);

        when(invoiceApi.generateInvoice(any())).thenThrow(new RuntimeException("PDF generation failed"));

        assertThrows(RuntimeException.class, () -> invoiceDto.generate(form));
    }

    @Test
    public void testGenerateWithNullBase64Response() {
        InvoiceForm form = new InvoiceForm();
        form.setOrderId(5);
        form.setClientName("Test Client");
        form.setItems(Collections.emptyList());
        form.setTotalAmount(BigDecimal.ZERO);

        when(invoiceApi.generateInvoice(any())).thenReturn(null);

        InvoiceData result = invoiceDto.generate(form);

        assertNull(result.getBase64Pdf());
    }

    @Test
    public void testGenerateWithEmptyBase64Response() {
        InvoiceForm form = new InvoiceForm();
        form.setOrderId(6);
        form.setClientName("Test Client");
        form.setItems(Collections.emptyList());
        form.setTotalAmount(BigDecimal.ZERO);

        when(invoiceApi.generateInvoice(any())).thenReturn("");

        InvoiceData result = invoiceDto.generate(form);

        assertEquals("", result.getBase64Pdf());
    }
}

