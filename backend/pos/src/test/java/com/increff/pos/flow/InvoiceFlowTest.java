package com.increff.pos.flow;

import com.increff.pos.api.*;
import com.increff.pos.domain.OrderStatus;
import com.increff.pos.entity.*;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import com.increff.pos.model.data.InvoiceData;
import com.increff.pos.model.form.InvoiceForm;
import com.increff.pos.model.form.InvoiceItemForm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceFlowTest {

    @Mock
    private OrderApi orderApi;

    @Mock
    private OrderItemApi orderItemApi;

    @Mock
    private InvoiceApi invoiceApi;

    @Mock
    private ClientApi clientApi;

    @Mock
    private ProductApi productApi;

    @InjectMocks
    private InvoiceFlow invoiceFlow;

    @Test
    void should_generate_invoice_successfully() {
        // Arrange
        Integer orderId = 1;
        
        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        order.setClientId(1);
        order.setCreatedAt(ZonedDateTime.now());
        order.setStatus(OrderStatus.CREATED);

        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setProductId(1);
        orderItem.setQuantity(2);
        orderItem.setSellingPrice(new BigDecimal("50.00"));

        ProductEntity product = new ProductEntity();
        product.setId(1);
        product.setProductName("Test Product");

        ClientEntity client = new ClientEntity();
        client.setId(1);
        client.setClientName("Test Client");

        InvoiceData invoiceData = new InvoiceData();
        invoiceData.setBase64Pdf("dGVzdCBwZGYgY29udGVudA=="); // base64 encoded "test pdf content"

        InvoiceEntity savedInvoice = new InvoiceEntity();
        savedInvoice.setOrderId(orderId);
        savedInvoice.setFilePath("invoices/invoice-1.pdf");

        when(invoiceApi.existsForOrder(orderId)).thenReturn(false);
        when(orderApi.getById(orderId)).thenReturn(order);
        when(orderItemApi.getByOrderId(orderId)).thenReturn(List.of(orderItem));
        when(productApi.getByIds(List.of(1))).thenReturn(List.of(product));
        when(clientApi.getById(1)).thenReturn(client);
        when(invoiceApi.generate(any(InvoiceForm.class))).thenReturn(invoiceData);
        when(invoiceApi.create(any(InvoiceEntity.class))).thenReturn(savedInvoice);
        when(orderApi.update(any(OrderEntity.class))).thenReturn(order);

        // Act
        InvoiceEntity result = invoiceFlow.generateInvoice(orderId);

        // Assert
        assertNotNull(result);
        assertEquals(orderId, result.getOrderId());
        assertEquals("invoices/invoice-1.pdf", result.getFilePath());
        verify(invoiceApi).existsForOrder(orderId);
        verify(orderApi).getById(orderId);
        verify(orderItemApi).getByOrderId(orderId);
        verify(invoiceApi).generate(any(InvoiceForm.class));
        verify(invoiceApi).create(any(InvoiceEntity.class));
        verify(orderApi).update(order);
        assertEquals(OrderStatus.INVOICED, order.getStatus());
    }

    @Test
    void should_throw_exception_when_generating_invoice_already_exists() {
        // Arrange
        Integer orderId = 1;
        when(invoiceApi.existsForOrder(orderId)).thenReturn(true);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> invoiceFlow.generateInvoice(orderId));
        assertEquals(ApiStatus.ORDER_ALREADY_INVOICED, exception.getStatus());
        assertTrue(exception.getMessage().contains("Invoice already generated for order " + orderId));
        verify(invoiceApi).existsForOrder(orderId);
        verifyNoMoreInteractions(invoiceApi, orderApi, orderItemApi, clientApi, productApi);
    }

    @Test
    void should_download_invoice_successfully() {
        // Arrange
        Integer orderId = 1;
        byte[] expectedPdf = "test pdf content".getBytes();
        
        InvoiceEntity invoice = new InvoiceEntity();
        invoice.setOrderId(orderId);
        invoice.setFilePath("invoices/invoice-1.pdf");

        when(invoiceApi.downloadInvoice(orderId)).thenReturn(expectedPdf);

        // Act
        byte[] result = invoiceFlow.downloadInvoice(orderId);

        // Assert
        assertArrayEquals(expectedPdf, result);
        verify(invoiceApi).downloadInvoice(orderId);
    }

    @Test
    void should_build_invoice_form_with_single_item() {
        // Arrange
        OrderEntity order = new OrderEntity();
        order.setId(1);
        order.setClientId(1);
        order.setCreatedAt(ZonedDateTime.now());

        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setProductId(1);
        orderItem.setQuantity(2);
        orderItem.setSellingPrice(new BigDecimal("50.00"));

        ProductEntity product = new ProductEntity();
        product.setId(1);
        product.setProductName("Test Product");

        ClientEntity client = new ClientEntity();
        client.setId(1);
        client.setClientName("Test Client");

        when(productApi.getByIds(List.of(1))).thenReturn(List.of(product));
        when(clientApi.getById(1)).thenReturn(client);

        // Use reflection to test private method
        try {
            java.lang.reflect.Method method = InvoiceFlow.class.getDeclaredMethod("buildInvoiceForm", 
                OrderEntity.class, List.class);
            method.setAccessible(true);
            
            // Act
            InvoiceForm result = (InvoiceForm) method.invoke(invoiceFlow, order, List.of(orderItem));

            // Assert
            assertNotNull(result);
            assertEquals("INV-1", result.getInvoiceNumber());
            assertEquals(order.getCreatedAt(), result.getInvoiceDate());
            assertEquals("Test Client", result.getClientName());
            assertNotNull(result.getItems());
            assertEquals(1, result.getItems().size());
            
            InvoiceItemForm item = result.getItems().get(0);
            assertEquals("Test Product", item.getProductName());
            assertEquals(2, item.getQuantity());
            assertEquals(new BigDecimal("50.00"), item.getSellingPrice());
            assertEquals(new BigDecimal("100.00"), item.getLineTotal());
            assertEquals(new BigDecimal("100.00"), result.getTotalAmount());
            
        } catch (Exception e) {
            fail("Failed to test private method: " + e.getMessage());
        }
    }

    @Test
    void should_build_invoice_form_with_multiple_items() {
        // Arrange
        OrderEntity order = new OrderEntity();
        order.setId(1);
        order.setClientId(1);
        order.setCreatedAt(ZonedDateTime.now());

        OrderItemEntity item1 = new OrderItemEntity();
        item1.setProductId(1);
        item1.setQuantity(2);
        item1.setSellingPrice(new BigDecimal("50.00"));

        OrderItemEntity item2 = new OrderItemEntity();
        item2.setProductId(2);
        item2.setQuantity(1);
        item2.setSellingPrice(new BigDecimal("30.00"));

        ProductEntity product1 = new ProductEntity();
        product1.setId(1);
        product1.setProductName("Product 1");

        ProductEntity product2 = new ProductEntity();
        product2.setId(2);
        product2.setProductName("Product 2");

        ClientEntity client = new ClientEntity();
        client.setId(1);
        client.setClientName("Test Client");

        when(productApi.getByIds(List.of(1, 2))).thenReturn(List.of(product1, product2));
        when(clientApi.getById(1)).thenReturn(client);

        // Use reflection to test private method
        try {
            java.lang.reflect.Method method = InvoiceFlow.class.getDeclaredMethod("buildInvoiceForm", 
                OrderEntity.class, List.class);
            method.setAccessible(true);
            
            // Act
            InvoiceForm result = (InvoiceForm) method.invoke(invoiceFlow, order, List.of(item1, item2));

            // Assert
            assertNotNull(result);
            assertEquals("INV-1", result.getInvoiceNumber());
            assertEquals("Test Client", result.getClientName());
            assertEquals(2, result.getItems().size());
            assertEquals(new BigDecimal("130.00"), result.getTotalAmount());
            
        } catch (Exception e) {
            fail("Failed to test private method: " + e.getMessage());
        }
    }

    @Test
    void should_save_pdf_successfully() {
        // Arrange
        Integer orderId = 1;
        String base64Pdf = "dGVzdCBwZGYgY29udGVudA=="; // base64 encoded "test pdf content"

        // Use reflection to test private method
        try {
            java.lang.reflect.Method method = InvoiceFlow.class.getDeclaredMethod("savePdf", Integer.class, String.class);
            method.setAccessible(true);
            
            // Act
            String result = (String) method.invoke(invoiceFlow, orderId, base64Pdf);

            // Assert
            assertEquals("invoices/invoice-1.pdf", result);
            
        } catch (Exception e) {
            fail("Failed to test private method: " + e.getMessage());
        }
    }

    @Test
    void should_handle_duplicate_product_ids_in_order_items() {
        // Arrange
        OrderEntity order = new OrderEntity();
        order.setId(1);
        order.setClientId(1);
        order.setCreatedAt(ZonedDateTime.now());

        OrderItemEntity item1 = new OrderItemEntity();
        item1.setProductId(1);
        item1.setQuantity(2);
        item1.setSellingPrice(new BigDecimal("50.00"));

        OrderItemEntity item2 = new OrderItemEntity();
        item2.setProductId(1); // Same product ID
        item2.setQuantity(1);
        item2.setSellingPrice(new BigDecimal("50.00"));

        ProductEntity product = new ProductEntity();
        product.setId(1);
        product.setProductName("Test Product");

        ClientEntity client = new ClientEntity();
        client.setId(1);
        client.setClientName("Test Client");

        when(productApi.getByIds(List.of(1))).thenReturn(List.of(product));
        when(clientApi.getById(1)).thenReturn(client);

        // Use reflection to test private method
        try {
            java.lang.reflect.Method method = InvoiceFlow.class.getDeclaredMethod("buildInvoiceForm", 
                OrderEntity.class, List.class);
            method.setAccessible(true);
            
            // Act
            InvoiceForm result = (InvoiceForm) method.invoke(invoiceFlow, order, List.of(item1, item2));

            // Assert
            assertNotNull(result);
            assertEquals(2, result.getItems().size()); // Should have 2 items even with same product ID
            assertEquals(new BigDecimal("150.00"), result.getTotalAmount()); // (2+1) * 50.00
            
        } catch (Exception e) {
            fail("Failed to test private method: " + e.getMessage());
        }
    }
}
