package com.increff.invoice.dto;

import com.increff.invoice.api.InvoiceApi;
import com.increff.invoice.model.data.InvoiceData;
import com.increff.invoice.model.form.InvoiceForm;
import com.increff.invoice.model.internal.InvoiceModel;
import com.increff.invoice.util.ConversionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InvoiceDto extends AbstractDto{

    @Autowired
    private InvoiceApi invoiceApi;

    public InvoiceData generate(InvoiceForm form) {
        checkValid(form);
        InvoiceModel model = ConversionUtil.convertInvoiceFormToModel(form);
        String base64 = invoiceApi.generateInvoice(model);

        InvoiceData data = new InvoiceData();
        data.setBase64Pdf(base64);
        return data;
    }

}
