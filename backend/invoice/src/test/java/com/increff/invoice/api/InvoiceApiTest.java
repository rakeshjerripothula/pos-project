package com.increff.invoice.api;

import com.increff.invoice.model.internal.InvoiceModel;
import com.increff.invoice.service.PdfGenerationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InvoiceApiTest {

    @Mock
    private PdfGenerationService pdfGenerationService;

    @InjectMocks
    private InvoiceApi invoiceApi;

    @Test
    public void testGenerateInvoice() {
        InvoiceModel model = new InvoiceModel();
        String expectedPdf = "base64pdfcontent";
        
        when(pdfGenerationService.generatePdf(any(InvoiceModel.class))).thenReturn(expectedPdf);
        
        String result = invoiceApi.generateInvoice(model);
        
        assertEquals(expectedPdf, result);
    }
}
