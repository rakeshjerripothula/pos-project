package com.increff.pos.dto;

import com.increff.pos.domain.OrderStatus;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.OrderData;
import com.increff.pos.model.data.OrderItemData;
import com.increff.pos.model.data.OrderPageData;
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
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrderDtoTest {

    @Mock
    private com.increff.pos.flow.OrderFlow orderFlow;

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
        com.increff.pos.entity.OrderItemEntity orderItem = createOrderItemEntity(1, 1, 2, new BigDecimal("10.99"));
        com.increff.pos.entity.ProductEntity product = createProductEntity(1, "Test Product", new BigDecimal("15.99"));
        
        when(orderItemApi.getByOrderId(orderId)).thenReturn(Arrays.asList(orderItem));
        when(productApi.getByIds(Arrays.asList(1))).thenReturn(Arrays.asList(product));

        // Act
        List<OrderItemData> result = orderDto.getOrderItems(orderId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getProductName());
        assertEquals(2, result.get(0).getQuantity());
        verify(orderItemApi, times(1)).getByOrderId(orderId);
        verify(productApi, times(1)).getByIds(Arrays.asList(1));
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

    private com.increff.pos.entity.ProductEntity createProductEntity(Integer id, String name, BigDecimal mrp) {
        com.increff.pos.entity.ProductEntity product = new com.increff.pos.entity.ProductEntity();
        product.setId(id);
        product.setProductName(name);
        product.setMrp(mrp);
        return product;
    }
}
