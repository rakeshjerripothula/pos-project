package com.increff.pos.dao;

import com.increff.pos.entity.InvoiceEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

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

        invoiceDao.insert(invoice);

        InvoiceEntity found = invoiceDao.selectByOrderId(123);
        
        assertNotNull(found);
        assertEquals(123, found.getOrderId());
        assertEquals("/invoices/invoice-123.pdf", found.getFilePath());
    }

    @Test
    void testSelectByOrderId() {
        InvoiceEntity invoice = new InvoiceEntity();
        invoice.setOrderId(456);
        invoice.setFilePath("/invoices/invoice-456.pdf");

        invoiceDao.insert(invoice);

        InvoiceEntity found = invoiceDao.selectByOrderId(456);

        assertNotNull(found);
        assertEquals(456, found.getOrderId());
        assertEquals("/invoices/invoice-456.pdf", found.getFilePath());

        InvoiceEntity notFound = invoiceDao.selectByOrderId(999);
        assertNull(notFound);
    }
}
