package com.increff.invoice.util;

import com.increff.invoice.model.form.InvoiceForm;
import com.increff.invoice.model.form.InvoiceItemForm;
import com.increff.invoice.model.internal.InvoiceItemModel;
import com.increff.invoice.model.internal.InvoiceModel;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.stream.Collectors;

public class ConversionUtil {

    public static InvoiceModel convertInvoiceFormToModel(InvoiceForm form) {
        InvoiceModel model = new InvoiceModel();
        model.setInvoiceNumber("INV-" + form.getOrderId());
        model.setInvoiceDate(ZonedDateTime.now());
        model.setClientName(form.getClientName());
        model.setTotalAmount(form.getTotalAmount());

        model.setItems(form.getItems() != null ? 
            form.getItems().stream().map(ConversionUtil::convertInvoiceItemToModel).collect(Collectors.toList()) :
            Collections.emptyList());
        return model;
    }

    public static InvoiceItemModel convertInvoiceItemToModel(InvoiceItemForm form) {
        InvoiceItemModel model = new InvoiceItemModel();
        model.setProductName(form.getProductName());
        model.setQuantity(form.getQuantity());
        model.setSellingPrice(form.getSellingPrice());
        model.setLineTotal(form.getLineTotal());
        return model;
    }
}
