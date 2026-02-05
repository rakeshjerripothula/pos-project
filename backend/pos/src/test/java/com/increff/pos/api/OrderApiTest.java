package com.increff.pos.api;

import com.increff.pos.dao.OrderDao;
import com.increff.pos.entity.OrderEntity;
import com.increff.pos.domain.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderApiTest {

    @Mock
    private OrderDao orderDao;

    @InjectMocks
    private OrderApi orderApi;

    @Test
    void should_create_order_when_valid_input() {
        // Arrange
        OrderEntity order = new OrderEntity();
        order.setClientId(1);
        order.setStatus(OrderStatus.CREATED);

        when(orderDao.save(order)).thenAnswer(invocation -> {
            OrderEntity saved = invocation.getArgument(0);
            saved.setId(1);
            return saved;
        });

        // Act
        OrderEntity result = orderApi.create(order);

        // Assert
        assertEquals(1, result.getId());
        assertEquals(1, result.getClientId());
        assertEquals(OrderStatus.CREATED, result.getStatus());
        verify(orderDao).save(order);
    }

    @Test
    void should_get_order_by_id_when_exists() {
        // Arrange
        OrderEntity order = new OrderEntity();
        order.setId(1);
        order.setClientId(1);
        order.setStatus(OrderStatus.CREATED);

        when(orderDao.findById(1)).thenReturn(Optional.of(order));

        // Act
        OrderEntity result = orderApi.getById(1);

        // Assert
        assertEquals(1, result.getId());
        assertEquals(1, result.getClientId());
        assertEquals(OrderStatus.CREATED, result.getStatus());
        verify(orderDao).findById(1);
    }

    @Test
    void should_throw_exception_when_getting_order_by_id_not_found() {
        // Arrange
        when(orderDao.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> orderApi.getById(1));
        assertEquals("Order not found: 1", exception.getMessage());
        verify(orderDao).findById(1);
    }

    @Test
    void should_update_order_when_valid_input() {
        // Arrange
        OrderEntity order = new OrderEntity();
        order.setId(1);
        order.setClientId(1);
        order.setStatus(OrderStatus.INVOICED);

        when(orderDao.save(order)).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderEntity result = orderApi.update(order);

        // Assert
        assertEquals(1, result.getId());
        assertEquals(1, result.getClientId());
        assertEquals(OrderStatus.INVOICED, result.getStatus());
        verify(orderDao).save(order);
    }

    @Test
    void should_search_orders_with_all_parameters() {
        // Arrange
        OrderStatus status = OrderStatus.CREATED;
        Integer clientId = 1;
        ZonedDateTime start = ZonedDateTime.now().minusDays(1);
        ZonedDateTime end = ZonedDateTime.now().plusDays(1);
        Integer page = 0;
        Integer pageSize = 10;

        List<OrderEntity> orders = List.of(new OrderEntity(), new OrderEntity());
        Page<OrderEntity> orderPage = new PageImpl<>(orders);

        when(orderDao.search(eq(status), eq(clientId), eq(start), eq(end), any(Pageable.class)))
            .thenReturn(orderPage);

        // Act
        Page<OrderEntity> result = orderApi.search(status, clientId, start, end, page, pageSize);

        // Assert
        assertEquals(2, result.getContent().size());
        verify(orderDao).search(eq(status), eq(clientId), eq(start), eq(end), any(Pageable.class));
    }

    @Test
    void should_search_orders_with_null_parameters() {
        // Arrange
        Integer page = 0;
        Integer pageSize = 10;

        List<OrderEntity> orders = List.of(new OrderEntity());
        Page<OrderEntity> orderPage = new PageImpl<>(orders);

        when(orderDao.search(isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(orderPage);

        // Act
        Page<OrderEntity> result = orderApi.search(null, null, null, null, page, pageSize);

        // Assert
        assertEquals(1, result.getContent().size());
        verify(orderDao).search(isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void should_search_orders_with_partial_parameters() {
        // Arrange
        OrderStatus status = OrderStatus.INVOICED;
        Integer page = 0;
        Integer pageSize = 5;

        List<OrderEntity> orders = List.of(new OrderEntity());
        Page<OrderEntity> orderPage = new PageImpl<>(orders);

        when(orderDao.search(eq(status), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(orderPage);

        // Act
        Page<OrderEntity> result = orderApi.search(status, null, null, null, page, pageSize);

        // Assert
        assertEquals(1, result.getContent().size());
        verify(orderDao).search(eq(status), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void should_search_orders_with_different_page_sizes() {
        // Arrange
        Integer page = 1;
        Integer pageSize = 20;

        List<OrderEntity> orders = List.of(new OrderEntity(), new OrderEntity(), new OrderEntity());
        Page<OrderEntity> orderPage = new PageImpl<>(orders);

        when(orderDao.search(isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(orderPage);

        // Act
        Page<OrderEntity> result = orderApi.search(null, null, null, null, page, pageSize);

        // Assert
        assertEquals(3, result.getContent().size());
        verify(orderDao).search(isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void should_handle_empty_search_results() {
        // Arrange
        Integer page = 0;
        Integer pageSize = 10;

        Page<OrderEntity> emptyPage = new PageImpl<>(List.of());

        when(orderDao.search(isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(emptyPage);

        // Act
        Page<OrderEntity> result = orderApi.search(null, null, null, null, page, pageSize);

        // Assert
        assertEquals(0, result.getContent().size());
        assertEquals(0, result.getTotalElements());
        verify(orderDao).search(isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void should_create_order_with_null_id() {
        // Arrange
        OrderEntity order = new OrderEntity();
        order.setClientId(1);
        order.setStatus(OrderStatus.CREATED);
        order.setId(null);

        when(orderDao.save(order)).thenAnswer(invocation -> {
            OrderEntity saved = invocation.getArgument(0);
            saved.setId(1);
            return saved;
        });

        // Act
        OrderEntity result = orderApi.create(order);

        // Assert
        assertEquals(1, result.getId());
        verify(orderDao).save(order);
    }

    @Test
    void should_update_existing_order() {
        // Arrange
        OrderEntity order = new OrderEntity();
        order.setId(1);
        order.setClientId(1);
        order.setStatus(OrderStatus.CREATED);

        when(orderDao.save(order)).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderEntity result = orderApi.update(order);

        // Assert
        assertEquals(1, result.getId());
        verify(orderDao).save(order);
    }

    @Test
    void should_verify_pageable_creation_in_search() {
        // Arrange
        Integer page = 2;
        Integer pageSize = 15;

        List<OrderEntity> orders = List.of(new OrderEntity());
        Page<OrderEntity> orderPage = new PageImpl<>(orders);

        when(orderDao.search(isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(orderPage);

        // Act
        Page<OrderEntity> result = orderApi.search(null, null, null, null, page, pageSize);

        // Assert
        assertEquals(1, result.getContent().size());
        verify(orderDao).search(isNull(), isNull(), isNull(), isNull(), argThat(pageable -> 
            pageable.getPageNumber() == 2 && pageable.getPageSize() == 15
        ));
    }
}
