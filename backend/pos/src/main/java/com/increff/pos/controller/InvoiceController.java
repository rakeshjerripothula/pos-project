package com.increff.pos.controller;

import com.increff.pos.model.data.InvoiceSummaryData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import com.increff.pos.dto.InvoiceDto;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class InvoiceController {

    @Autowired
    private InvoiceDto invoiceDto;

    @PostMapping("/{id}/invoice")
    public InvoiceSummaryData generateInvoice(@PathVariable Integer id) {
        return invoiceDto.generateInvoice(id);
    }

    @GetMapping("/{id}/invoice/download")
    public ResponseEntity<Resource> downloadInvoice(@PathVariable Integer id) {
        byte[] pdfBytes = invoiceDto.downloadInvoice(id);
        
        ByteArrayResource resource = new ByteArrayResource(pdfBytes);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF).contentLength(pdfBytes.length).body(resource);
    }
}