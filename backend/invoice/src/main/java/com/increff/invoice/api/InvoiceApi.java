package com.increff.invoice.api;

import com.increff.invoice.model.internal.InvoiceModel;
import com.increff.invoice.service.PdfGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InvoiceApi {

    @Autowired
    private PdfGenerationService pdfGenerationService;

    public String generateInvoice(InvoiceModel model) {
        return pdfGenerationService.generatePdf(model);
    }
}
