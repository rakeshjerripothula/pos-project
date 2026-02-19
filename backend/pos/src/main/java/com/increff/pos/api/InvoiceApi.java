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

    @Transactional
    public void create(InvoiceEntity invoice) {
        invoiceDao.save(invoice);
    }

    @Transactional(readOnly = true)
    public boolean existsForOrder(Integer orderId) {
        return invoiceDao.selectByOrderId(orderId).isPresent();
    }

    @Transactional(readOnly = true)
    public InvoiceEntity getCheckByOrderId(Integer orderId) {
        return invoiceDao.selectByOrderId(orderId)
                .orElseThrow(() -> new ApiException(ApiStatus.NOT_FOUND, "Invoice not found for order " + orderId));
    }

    @Transactional(readOnly = true)
    public byte[] download(Integer orderId) {
        InvoiceEntity invoice = getCheckByOrderId(orderId);
        try {
            return java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(invoice.getFilePath()));
        } catch (Exception e) {
            throw new ApiException(ApiStatus.INTERNAL_ERROR, "Failed to read invoice file");
        }
    }
}
