package com.increff.pos.api;

import com.increff.pos.dao.InvoiceDao;
import com.increff.pos.entity.InvoiceEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceApiTest {

    @Mock
    private InvoiceDao invoiceDao;

    @InjectMocks
    private InvoiceApi invoiceApi;

    @Test
    void should_create_invoice() {
        // Arrange
        InvoiceEntity invoice = new InvoiceEntity();
        invoice.setOrderId(1);
        invoice.setFilePath("/path/to/invoice.pdf");

        when(invoiceDao.save(any(InvoiceEntity.class))).thenReturn(invoice);

        // Act
        invoiceApi.create(invoice);

        // Assert
        verify(invoiceDao).save(invoice);
    }

    @Test
    void should_return_true_when_invoice_exists_for_order() {
        // Arrange
        InvoiceEntity existingInvoice = new InvoiceEntity();
        existingInvoice.setOrderId(1);

        when(invoiceDao.selectByOrderId(1)).thenReturn(Optional.of(existingInvoice));

        // Act
        boolean result = invoiceApi.existsForOrder(1);

        // Assert
        assertTrue(result);
        verify(invoiceDao).selectByOrderId(1);
    }

    @Test
    void should_return_false_when_invoice_does_not_exist_for_order() {
        // Arrange
        when(invoiceDao.selectByOrderId(1)).thenReturn(Optional.empty());

        // Act
        boolean result = invoiceApi.existsForOrder(1);

        // Assert
        assertFalse(result);
        verify(invoiceDao).selectByOrderId(1);
    }

    @Test
    void should_get_invoice_by_order_id_when_exists() {
        // Arrange
        InvoiceEntity invoice = new InvoiceEntity();
        invoice.setOrderId(123);
        invoice.setFilePath("/path/to/invoice.pdf");

        when(invoiceDao.selectByOrderId(123)).thenReturn(Optional.of(invoice));

        // Act
        InvoiceEntity result = invoiceApi.getCheckByOrderId(123);

        // Assert
        assertEquals(123, result.getOrderId());
        assertEquals("/path/to/invoice.pdf", result.getFilePath());
        verify(invoiceDao).selectByOrderId(123);
    }

    @Test
    void should_throw_exception_when_getting_invoice_by_nonexistent_order_id() {
        // Arrange
        when(invoiceDao.selectByOrderId(999)).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> invoiceApi.getCheckByOrderId(999));
        assertEquals(ApiStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Invoice not found for order 999", exception.getMessage());
        verify(invoiceDao).selectByOrderId(999);
    }

    @Test
    void should_download_invoice_bytes_when_file_exists() throws IOException {
        // Arrange
        String filePath = "/tmp/test-invoice.pdf";
        byte[] expectedBytes = "PDF content".getBytes();
        
        InvoiceEntity invoice = new InvoiceEntity();
        invoice.setOrderId(1);
        invoice.setFilePath(filePath);

        // Create a temporary file for testing
        Path tempFile = Paths.get(filePath);
        Files.write(tempFile, expectedBytes);

        when(invoiceDao.selectByOrderId(1)).thenReturn(Optional.of(invoice));

        try {
            // Act
            byte[] result = invoiceApi.download(1);

            // Assert
            assertArrayEquals(expectedBytes, result);
            verify(invoiceDao).selectByOrderId(1);
        } finally {
            // Cleanup
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void should_throw_exception_when_downloading_invoice_file_not_found() {
        // Arrange
        InvoiceEntity invoice = new InvoiceEntity();
        invoice.setOrderId(1);
        invoice.setFilePath("/nonexistent/path/invoice.pdf");

        when(invoiceDao.selectByOrderId(1)).thenReturn(Optional.of(invoice));

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> invoiceApi.download(1));
        assertEquals(ApiStatus.INTERNAL_ERROR, exception.getStatus());
        assertEquals("Failed to read invoice file", exception.getMessage());
        verify(invoiceDao).selectByOrderId(1);
    }
}
