package com.increff.invoice.controller;

import com.increff.invoice.dto.InvoiceDto;
import com.increff.invoice.model.data.InvoiceData;
import com.increff.invoice.model.form.InvoiceForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invoice")
public class InvoiceController {

    @Autowired
    private InvoiceDto invoiceDto;

    @PostMapping("/generate")
    public InvoiceData generate(@RequestBody InvoiceForm form) {
        return invoiceDto.generate(form);
    }
}
