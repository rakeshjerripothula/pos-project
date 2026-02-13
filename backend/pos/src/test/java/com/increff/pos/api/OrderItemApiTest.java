package com.increff.pos.api;

import com.increff.pos.dao.OrderItemDao;
import com.increff.pos.entity.OrderItemEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderItemApiTest {

    @Mock
    private OrderItemDao orderItemDao;

    @InjectMocks
    private OrderItemApi orderItemApi;

    @Test
    void should_create_all_order_items() {
        // Arrange
        OrderItemEntity item1 = new OrderItemEntity();
        item1.setOrderId(1);
        item1.setProductId(1);
        item1.setQuantity(2);
        item1.setSellingPrice(new BigDecimal("100.00"));

        OrderItemEntity item2 = new OrderItemEntity();
        item2.setOrderId(1);
        item2.setProductId(2);
        item2.setQuantity(3);
        item2.setSellingPrice(new BigDecimal("150.00"));

        List<OrderItemEntity> items = List.of(item1, item2);

        when(orderItemDao.saveAll(any(List.class))).thenReturn(items);

        // Act
        orderItemApi.createAll(items);

        // Assert
        verify(orderItemDao).saveAll(items);
    }

    @Test
    void should_get_order_items_by_order_id() {
        // Arrange
        OrderItemEntity item1 = new OrderItemEntity();
        item1.setId(1);
        item1.setOrderId(123);
        item1.setProductId(1);
        item1.setQuantity(2);
        item1.setSellingPrice(new BigDecimal("100.00"));

        OrderItemEntity item2 = new OrderItemEntity();
        item2.setId(2);
        item2.setOrderId(123);
        item2.setProductId(2);
        item2.setQuantity(3);
        item2.setSellingPrice(new BigDecimal("150.00"));

        List<OrderItemEntity> expectedItems = List.of(item1, item2);

        when(orderItemDao.findByOrderId(123)).thenReturn(expectedItems);

        // Act
        List<OrderItemEntity> result = orderItemApi.getByOrderId(123);

        // Assert
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(123, result.get(0).getOrderId());
        assertEquals(2, result.get(1).getId());
        assertEquals(123, result.get(1).getOrderId());
        verify(orderItemDao).findByOrderId(123);
    }

    @Test
    void should_return_empty_list_when_no_order_items_found_for_order_id() {
        // Arrange
        when(orderItemDao.findByOrderId(999)).thenReturn(List.of());

        // Act
        List<OrderItemEntity> result = orderItemApi.getByOrderId(999);

        // Assert
        assertEquals(0, result.size());
        verify(orderItemDao).findByOrderId(999);
    }
}
