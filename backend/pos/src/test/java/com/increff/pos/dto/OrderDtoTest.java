package com.increff.pos.dto;

import com.increff.pos.domain.OrderStatus;
import com.increff.pos.entity.InvoiceEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.InvoiceData;
import com.increff.pos.model.data.InvoiceSummaryData;
import com.increff.pos.model.data.OrderData;
import com.increff.pos.model.data.OrderItemData;
import com.increff.pos.model.data.OrderPageData;
import com.increff.pos.model.form.InvoiceForm;
import com.increff.pos.model.form.OrderForm;
import com.increff.pos.model.form.OrderItemForm;
import com.increff.pos.model.form.OrderPageForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrderDtoTest {

    @Mock
    private com.increff.pos.flow.OrderFlow orderFlow;

    @Mock
    private com.increff.pos.client.InvoiceClient invoiceClient;

    @Mock
    private com.increff.pos.api.OrderItemApi orderItemApi;

    @Mock
    private com.increff.pos.api.ProductApi productApi;

    @InjectMocks
    private OrderDto orderDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createOrder_validForm_success() {
        // Arrange
        OrderForm form = new OrderForm();
        OrderItemForm itemForm = new OrderItemForm();
        itemForm.setProductId(1);
        itemForm.setQuantity(2);
        itemForm.setSellingPrice(new BigDecimal("10.99"));
        form.setItems(Arrays.asList(itemForm));
        
        com.increff.pos.entity.OrderEntity savedOrder = createOrderEntity(1, OrderStatus.CREATED);
        when(orderFlow.createOrder(any())).thenReturn(savedOrder);

        // Act
        OrderData result = orderDto.create(form);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getOrderId());
        assertEquals(OrderStatus.CREATED, result.getStatus());
        verify(orderFlow, times(1)).createOrder(any());
    }

    @Test
    void createOrder_invalidItems_throwsException() {
        // Arrange
        OrderForm form = new OrderForm();
        form.setItems(Arrays.asList()); // Empty items

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> orderDto.create(form));
        assertEquals("BAD_DATA", exception.getStatus().name());
        assertTrue(exception.hasErrors());
        assertTrue(exception.getErrors().get(0).getMessage().contains("Order must contain at least one item"));
        verify(orderFlow, never()).createOrder(any());
    }

    @Test
    void createOrder_nullForm_throwsException() {
        // Act & Assert
        assertThrows(ApiException.class, () -> orderDto.create(null));
        verify(orderFlow, never()).createOrder(any());
    }

    @Test
    void cancel_validId_success() {
        // Arrange
        Integer orderId = 1;
        com.increff.pos.entity.OrderEntity cancelledOrder = createOrderEntity(orderId, OrderStatus.CANCELLED);
        when(orderFlow.cancelOrder(orderId)).thenReturn(cancelledOrder);

        // Act
        OrderData result = orderDto.cancel(orderId);

        // Assert
        assertNotNull(result);
        assertEquals(orderId, result.getOrderId());
        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(orderFlow, times(1)).cancelOrder(orderId);
    }

    @Test
    void cancel_invalidId_throwsException() {
        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> orderDto.cancel(null));
        assertEquals("BAD_DATA", exception.getStatus().name());
        assertTrue(exception.getMessage().contains("Order ID is required"));
        verify(orderFlow, never()).cancelOrder(any());
    }

    @Test
    void getOrderItems_validId_success() {
        // Arrange
        Integer orderId = 1;
        OrderItemData orderItemData = new OrderItemData();
        orderItemData.setProductName("Test Product");
        orderItemData.setQuantity(2);
        orderItemData.setSellingPrice(new BigDecimal("10.99"));
        
        when(orderFlow.getOrderItems(orderId)).thenReturn(Arrays.asList(orderItemData));

        // Act
        List<OrderItemData> result = orderDto.getOrderItems(orderId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getProductName());
        assertEquals(2, result.get(0).getQuantity());
        verify(orderFlow, times(1)).getOrderItems(orderId);
    }

    @Test
    void getOrderItems_invalidId_throwsException() {
        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> orderDto.getOrderItems(null));
        assertEquals("BAD_DATA", exception.getStatus().name());
        assertTrue(exception.getMessage().contains("Order ID is required"));
        verify(orderItemApi, never()).getByOrderId(any());
    }

    @Test
    void getOrders_validForm_success() {
        // Arrange
        OrderPageForm form = new OrderPageForm();
        form.setPage(0);
        form.setPageSize(10);
        form.setStatus(OrderStatus.CREATED);
        
        com.increff.pos.entity.OrderEntity order = createOrderEntity(1, OrderStatus.CREATED);
        Page<com.increff.pos.entity.OrderEntity> orderPage = new PageImpl<>(Arrays.asList(order));
        
        when(orderFlow.searchOrders(eq(OrderStatus.CREATED), isNull(), isNull(), isNull(), eq(0), eq(10)))
            .thenReturn(orderPage);

        // Act
        OrderPageData result = orderDto.getOrders(form);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getPage());
        assertEquals(10, result.getPageSize());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(OrderStatus.CREATED, result.getContent().get(0).getStatus());
        verify(orderFlow, times(1)).searchOrders(eq(OrderStatus.CREATED), isNull(), isNull(), isNull(), eq(0), eq(10));
    }

    @Test
    void getOrders_invalidDateFormat_throwsException() {
        // Arrange
        OrderPageForm form = new OrderPageForm();
        form.setStartDate("invalid-date");

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> orderDto.getOrders(form));
        assertEquals("BAD_DATA", exception.getStatus().name());
        assertTrue(exception.getMessage().contains("Invalid date format"));
        verify(orderFlow, never()).searchOrders(any(), any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void testGetOrderItems() {
        // Arrange
        Integer orderId = 1;
        List<OrderItemData> expectedItems = Arrays.asList(
                createOrderItemData(1, 2, BigDecimal.valueOf(100.0)),
                createOrderItemData(2, 3, BigDecimal.valueOf(200.0))
        );

        when(orderFlow.getOrderItems(orderId)).thenReturn(expectedItems);

        // Act
        List<OrderItemData> result = orderDto.getOrderItems(orderId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getProductId());
        assertEquals(2, result.get(0).getQuantity());
        assertEquals(BigDecimal.valueOf(100.0), result.get(0).getSellingPrice());
        verify(orderFlow).getOrderItems(orderId);
    }

    @Test
    void testGetOrderItems_nullOrderId_throwsException() {
        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> orderDto.getOrderItems(null));
        assertEquals("BAD_DATA", exception.getStatus().name());
        assertTrue(exception.getMessage().contains("Order ID is required"));
        verify(orderFlow, never()).getOrderItems(any());
    }

    @Test
    void testGenerateInvoice() {
        // Arrange
        Integer orderId = 1;
        InvoiceForm invoiceForm = new InvoiceForm();
        InvoiceData invoiceData = new InvoiceData();
        invoiceData.setBase64Pdf("base64pdfcontent");
        
        InvoiceEntity invoiceEntity = new InvoiceEntity();
        invoiceEntity.setOrderId(orderId);
        invoiceEntity.setFilePath("/path/to/invoice.pdf");

        when(orderFlow.buildInvoiceForm(orderId)).thenReturn(invoiceForm);
        when(invoiceClient.generate(invoiceForm)).thenReturn(invoiceData);
        when(orderFlow.saveInvoice(eq(orderId), any())).thenReturn(invoiceEntity);

        // Act
        InvoiceSummaryData result = orderDto.generateInvoice(orderId);

        // Assert
        assertNotNull(result);
        verify(orderFlow).buildInvoiceForm(orderId);
        verify(invoiceClient).generate(invoiceForm);
        verify(orderFlow).saveInvoice(eq(orderId), any());
    }

    @Test
    void testDownloadInvoice() {
        // Arrange
        Integer orderId = 1;
        byte[] expectedPdf = "pdf content".getBytes();

        when(orderFlow.downloadInvoice(orderId)).thenReturn(expectedPdf);

        // Act
        byte[] result = orderDto.downloadInvoice(orderId);

        // Assert
        assertArrayEquals(expectedPdf, result);
        verify(orderFlow).downloadInvoice(orderId);
    }

    @Test
    void testDownloadInvoice_nullOrderId_throwsException() {
        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> orderDto.downloadInvoice(null));
        assertEquals("BAD_DATA", exception.getStatus().name());
        assertTrue(exception.getMessage().contains("Order ID is required"));
        verify(orderFlow, never()).downloadInvoice(any());
    }

    private com.increff.pos.entity.OrderEntity createOrderEntity(Integer id, OrderStatus status) {
        com.increff.pos.entity.OrderEntity order = new com.increff.pos.entity.OrderEntity();
        order.setId(id);
        order.setStatus(status);
        return order;
    }

    private com.increff.pos.entity.OrderItemEntity createOrderItemEntity(Integer id, Integer productId, Integer quantity, BigDecimal sellingPrice) {
        com.increff.pos.entity.OrderItemEntity item = new com.increff.pos.entity.OrderItemEntity();
        item.setId(id);
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setSellingPrice(sellingPrice);
        return item;
    }

    private OrderItemData createOrderItemData(Integer productId, Integer quantity, BigDecimal sellingPrice) {
        OrderItemData data = new OrderItemData();
        data.setProductId(productId);
        data.setQuantity(quantity);
        data.setSellingPrice(sellingPrice);
        return data;
    }

    private com.increff.pos.entity.ProductEntity createProductEntity(Integer id, String name, BigDecimal mrp) {
        com.increff.pos.entity.ProductEntity product = new com.increff.pos.entity.ProductEntity();
        product.setId(id);
        product.setProductName(name);
        product.setMrp(mrp);
        return product;
    }
}
