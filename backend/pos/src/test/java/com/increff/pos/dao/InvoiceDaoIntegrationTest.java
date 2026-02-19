package com.increff.pos.dao;

import com.increff.pos.entity.InvoiceEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class InvoiceDaoIntegrationTest {

    @Autowired
    private InvoiceDao invoiceDao;

    @Test
    void testInsert() {
        InvoiceEntity invoice = new InvoiceEntity();
        invoice.setOrderId(123);
        invoice.setFilePath("/invoices/invoice-123.pdf");

        invoiceDao.save(invoice);

        Optional<InvoiceEntity> found = invoiceDao.selectByOrderId(123);
        
        assertNotNull(found);
        assertEquals(123, found.get().getOrderId());
        assertEquals("/invoices/invoice-123.pdf", found.get().getFilePath());
    }

    @Test
    void testSelectByOrderId() {
        InvoiceEntity invoice = new InvoiceEntity();
        invoice.setOrderId(456);
        invoice.setFilePath("/invoices/invoice-456.pdf");

        invoiceDao.save(invoice);

        Optional<InvoiceEntity> found = invoiceDao.selectByOrderId(456);

        assertNotNull(found);
        assertEquals(456, found.get().getOrderId());
        assertEquals("/invoices/invoice-456.pdf", found.get().getFilePath());

        Optional<InvoiceEntity> notFound = invoiceDao.selectByOrderId(999);
        assertFalse(notFound.isPresent());
    }
}
