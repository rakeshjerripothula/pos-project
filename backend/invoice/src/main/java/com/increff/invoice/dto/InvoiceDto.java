package com.increff.invoice.dto;

import com.increff.invoice.api.InvoiceApi;
import com.increff.invoice.model.data.InvoiceData;
import com.increff.invoice.model.form.InvoiceForm;
import com.increff.invoice.model.form.InvoiceItemForm;
import com.increff.invoice.model.internal.InvoiceItemModel;
import com.increff.invoice.model.internal.InvoiceModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class InvoiceDto {

    @Autowired
    private InvoiceApi invoiceApi;

    public InvoiceData generate(InvoiceForm form) {

        InvoiceModel model = convertToInvoiceModel(form);
        String base64 = invoiceApi.generateInvoice(model);

        InvoiceData data = new InvoiceData();
        data.setBase64Pdf(base64);
        return data;
    }

    private InvoiceModel convertToInvoiceModel(InvoiceForm form) {
        InvoiceModel model = new InvoiceModel();
        model.setInvoiceNumber(form.getInvoiceNumber());
        model.setInvoiceDate(form.getInvoiceDate());
        model.setClientName(form.getClientName());
        model.setTotalAmount(form.getTotalAmount());

        model.setItems(form.getItems().stream().map(this::convertItemToInvoiceModel).collect(Collectors.toList()));
        return model;
    }

    private InvoiceItemModel convertItemToInvoiceModel(InvoiceItemForm form) {
        InvoiceItemModel model = new InvoiceItemModel();
        model.setProductName(form.getProductName());
        model.setQuantity(form.getQuantity());
        model.setSellingPrice(form.getSellingPrice());
        model.setLineTotal(form.getLineTotal());
        return model;
    }
}
