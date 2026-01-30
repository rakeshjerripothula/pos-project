package com.increff.pos.api;

import com.increff.pos.dao.InvoiceDao;
import com.increff.pos.entity.InvoiceEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import com.increff.pos.model.data.InvoiceData;
import com.increff.pos.model.form.InvoiceForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Service
@Transactional
public class InvoiceApi {

    @Autowired
    private InvoiceDao invoiceDao;

    @Autowired
    private RestTemplate restTemplate;

    private static final String INVOICE_APP_URL = "http://localhost:7070/invoice/generate";

    public InvoiceEntity create(InvoiceEntity invoice) {
        invoiceDao.insert(invoice);
        return invoice;
    }

    public boolean existsForOrder(Integer orderId) {
        return !Objects.isNull(invoiceDao.selectByOrderId(orderId));
    }

    public InvoiceEntity getByOrderId(Integer orderId) {
        InvoiceEntity invoice = invoiceDao.selectByOrderId(orderId);
        if (Objects.isNull(invoice)) {
            throw new ApiException(ApiStatus.NOT_FOUND, "Invoice not found for order " + orderId);
        }
        return invoice;
    }


    public InvoiceData generate(InvoiceForm form) {
        InvoiceData response = restTemplate.postForObject(
                INVOICE_APP_URL,
                form,
                InvoiceData.class
        );

        if (Objects.isNull(response)) {
            throw new ApiException(ApiStatus.INTERNAL_ERROR, "Failed to generate invoice: No response from invoice service");
        }

        return response;
    }

    public byte[] downloadInvoice(Integer orderId) {
        InvoiceEntity invoice = getByOrderId(orderId);
        
        try {
            java.nio.file.Path path = java.nio.file.Paths.get(invoice.getFilePath());
            return java.nio.file.Files.readAllBytes(path);
        } catch (Exception e) {
            throw new ApiException(ApiStatus.INTERNAL_ERROR, "Failed to read invoice file: " + e.getMessage());
        }
    }


}
