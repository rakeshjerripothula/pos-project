package com.increff.pos.client;

import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import com.increff.pos.model.form.InvoicePdfData;
import com.increff.pos.model.data.InvoiceClientForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class InvoiceClient {

    @Autowired
    private RestTemplate restTemplate;

    private static final String INVOICE_URL = "http://localhost:7070/invoice/generate";

    public InvoicePdfData generate(InvoiceClientForm form) {
        InvoicePdfData response = restTemplate.postForObject(INVOICE_URL, form, InvoicePdfData.class);

        if (response == null) {
            throw new ApiException(ApiStatus.INTERNAL_ERROR, "Invoice service returned null response");
        }

        return response;
    }
}
