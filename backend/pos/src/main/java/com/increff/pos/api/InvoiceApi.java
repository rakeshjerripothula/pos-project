package com.increff.pos.api;

import com.increff.pos.dao.InvoiceDao;
import com.increff.pos.entity.InvoiceEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InvoiceApi {

    @Autowired
    private InvoiceDao invoiceDao;

    public void create(InvoiceEntity invoice) {
        invoiceDao.insert(invoice);
    }

    public boolean existsForOrder(Integer orderId) {
        return invoiceDao.selectByOrderId(orderId) != null;
    }

    public InvoiceEntity getByOrderId(Integer orderId) {
        InvoiceEntity invoice = invoiceDao.selectByOrderId(orderId);
        if (invoice == null) {
            throw new ApiException(ApiStatus.NOT_FOUND, "Invoice not found for order " + orderId);
        }
        return invoice;
    }

    public byte[] download(Integer orderId) {
        InvoiceEntity invoice = getByOrderId(orderId);
        try {
            return java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(invoice.getFilePath()));
        } catch (Exception e) {
            throw new ApiException(ApiStatus.INTERNAL_ERROR, "Failed to read invoice file");
        }
    }
}
