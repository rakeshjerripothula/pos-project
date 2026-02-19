package com.increff.invoice.controller;

import com.increff.invoice.dto.InvoiceDto;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InvoiceControllerIntegrationTest {

    @Mock
    private InvoiceDto invoiceDto;

    @InjectMocks
    private InvoiceController invoiceController;

    @Test
    public void testGenerateInvoice() {
        InvoiceItemForm item = new InvoiceItemForm();
        item.setProductName("Test Product");
        item.setQuantity(1);
        item.setSellingPrice(new BigDecimal("100.00"));
        item.setLineTotal(new BigDecimal("100.00"));

        InvoiceForm form = new InvoiceForm();
        form.setOrderId(1);
        form.setClientName("Test Client");
        form.setItems(Collections.singletonList(item));
        form.setTotalAmount(new BigDecimal("100.00"));

        InvoiceData expectedData = new InvoiceData();
        expectedData.setBase64Pdf("base64pdfcontent");

        when(invoiceDto.generate(any(InvoiceForm.class))).thenReturn(expectedData);

        InvoiceData result = invoiceController.generate(form);

        assertEquals("base64pdfcontent", result.getBase64Pdf());
    }
}
