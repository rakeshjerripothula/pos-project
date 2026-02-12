package com.increff.pos.flow;

import com.increff.pos.api.*;
import com.increff.pos.entity.*;
import com.increff.pos.domain.OrderStatus;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderFlowTest {

    @Mock
    private OrderApi orderApi;

    @Mock
    private OrderItemApi orderItemApi;

    @Mock
    private InventoryApi inventoryApi;

    @Mock
    private ProductApi productApi;

    @Mock
    private ClientApi clientApi;

    @Mock
    private InventoryFlow inventoryFlow;

    @InjectMocks
    private OrderFlow orderFlow;

    @Test
    void should_create_order_when_valid_input() {
        // Arrange
        OrderItemEntity item1 = new OrderItemEntity();
        item1.setProductId(1);
        item1.setQuantity(2);
        item1.setSellingPrice(new BigDecimal("50.00"));

        OrderItemEntity item2 = new OrderItemEntity();
        item2.setProductId(2);
        item2.setQuantity(1);
        item2.setSellingPrice(new BigDecimal("30.00"));

        List<OrderItemEntity> items = List.of(item1, item2);

        ProductEntity product1 = new ProductEntity();
        product1.setId(1);
        product1.setClientId(1);
        product1.setMrp(new BigDecimal("60.00"));
        product1.setProductName("Product 1");

        ProductEntity product2 = new ProductEntity();
        product2.setId(2);
        product2.setClientId(1);
        product2.setMrp(new BigDecimal("40.00"));
        product2.setProductName("Product 2");

        InventoryEntity inventory1 = new InventoryEntity();
        inventory1.setProductId(1);
        inventory1.setQuantity(10);

        InventoryEntity inventory2 = new InventoryEntity();
        inventory2.setProductId(2);
        inventory2.setQuantity(5);

        OrderEntity order = new OrderEntity();
        order.setId(1);
        order.setClientId(1);
        order.setStatus(OrderStatus.CREATED);

        when(productApi.getByIds(List.of(1, 2))).thenReturn(List.of(product1, product2));
        when(clientApi.isClientEnabled(1)).thenReturn(true);
        when(orderApi.create(any(OrderEntity.class))).thenReturn(order);
        
        when(inventoryFlow.getInventoriesByProductIds(any())).thenReturn(List.of(inventory1, inventory2));

        // Act
        OrderEntity result = orderFlow.createOrder(items);

        // Assert
        assertEquals(1, result.getId());
        assertEquals(1, result.getClientId());
        assertEquals(OrderStatus.CREATED, result.getStatus());
        verify(productApi, times(1)).getByIds(List.of(1, 2));
        verify(clientApi).isClientEnabled(1);
        verify(inventoryFlow).getInventoriesByProductIds(any());
        verify(inventoryFlow).bulkUpsert(anyList());
        verify(orderApi).create(any(OrderEntity.class));
        verify(orderItemApi).createAll(anyList());
    }

    @Test
    void should_throw_exception_when_creating_order_with_empty_items() {
        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> orderFlow.createOrder(List.of()));
        assertEquals(ApiStatus.BAD_DATA, exception.getStatus());
        assertEquals("Order must contain at least one item", exception.getMessage());
        verifyNoInteractions(productApi, clientApi, inventoryApi, orderApi, orderItemApi);
    }

    @Test
    void should_throw_exception_when_creating_order_with_null_items() {
        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> orderFlow.createOrder(null));
        assertEquals(ApiStatus.BAD_DATA, exception.getStatus());
        assertEquals("Order must contain at least one item", exception.getMessage());
        verifyNoInteractions(productApi, clientApi, inventoryApi, orderApi, orderItemApi);
    }

    @Test
    void should_throw_exception_when_creating_order_with_selling_price_greater_than_mrp() {
        // Arrange
        OrderItemEntity item = new OrderItemEntity();
        item.setProductId(1);
        item.setQuantity(1);
        item.setSellingPrice(new BigDecimal("100.00")); // Greater than MRP

        ProductEntity product = new ProductEntity();
        product.setId(1);
        product.setMrp(new BigDecimal("80.00"));
        product.setProductName("Product 1");

        when(productApi.getByIds(List.of(1))).thenReturn(List.of(product));

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> orderFlow.createOrder(List.of(item)));
        assertEquals(ApiStatus.BAD_DATA, exception.getStatus());
        assertTrue(exception.getMessage().contains("Selling price cannot be greater than MRP"));
        verify(productApi, times(1)).getByIds(List.of(1));
        verifyNoInteractions(clientApi, inventoryApi, orderApi, orderItemApi);
    }

    @Test
    void should_throw_exception_when_creating_order_with_product_not_found() {
        // Arrange
        OrderItemEntity item = new OrderItemEntity();
        item.setProductId(1);
        item.setQuantity(1);
        item.setSellingPrice(new BigDecimal("50.00"));

        when(productApi.getByIds(List.of(1))).thenReturn(List.of());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> orderFlow.createOrder(List.of(item)));
        assertEquals(ApiStatus.NOT_FOUND, exception.getStatus());
        assertEquals("One or more products not found", exception.getMessage());
        verify(productApi, times(1)).getByIds(List.of(1));
        verifyNoInteractions(clientApi, inventoryApi, orderApi, orderItemApi);
    }

    @Test
    void should_throw_exception_when_creating_order_with_different_clients() {
        // Arrange
        OrderItemEntity item1 = new OrderItemEntity();
        item1.setProductId(1);
        item1.setQuantity(1);
        item1.setSellingPrice(new BigDecimal("50.00"));

        OrderItemEntity item2 = new OrderItemEntity();
        item2.setProductId(2);
        item2.setQuantity(1);
        item2.setSellingPrice(new BigDecimal("30.00"));

        ProductEntity product1 = new ProductEntity();
        product1.setId(1);
        product1.setClientId(1);
        product1.setMrp(new BigDecimal("60.00"));
        product1.setProductName("Product 1");

        ProductEntity product2 = new ProductEntity();
        product2.setId(2);
        product2.setClientId(2); // Different client
        product2.setMrp(new BigDecimal("40.00"));
        product2.setProductName("Product 2");

        when(productApi.getByIds(List.of(1, 2))).thenReturn(List.of(product1, product2));

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> orderFlow.createOrder(List.of(item1, item2)));
        assertEquals(ApiStatus.BAD_DATA, exception.getStatus());
        assertEquals("All products in an order must belong to the same client", exception.getMessage());
        verify(productApi, times(1)).getByIds(List.of(1, 2));
        verifyNoInteractions(clientApi, inventoryApi, orderApi, orderItemApi);
    }

    @Test
    void should_throw_exception_when_creating_order_for_disabled_client() {
        // Arrange
        OrderItemEntity item = new OrderItemEntity();
        item.setProductId(1);
        item.setQuantity(1);
        item.setSellingPrice(new BigDecimal("50.00"));

        ProductEntity product = new ProductEntity();
        product.setId(1);
        product.setClientId(1);
        product.setMrp(new BigDecimal("60.00"));
        product.setProductName("Product 1");

        when(productApi.getByIds(List.of(1))).thenReturn(List.of(product));
        when(clientApi.isClientEnabled(1)).thenReturn(false);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> orderFlow.createOrder(List.of(item)));
        assertEquals(ApiStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Client is disabled", exception.getMessage());
        verify(productApi, times(1)).getByIds(List.of(1));
        verify(clientApi).isClientEnabled(1);
        verifyNoInteractions(inventoryApi, orderApi, orderItemApi);
    }

    @Test
    void should_throw_exception_when_creating_order_with_insufficient_inventory() {
        // Arrange
        OrderItemEntity item = new OrderItemEntity();
        item.setProductId(1);
        item.setQuantity(10);
        item.setSellingPrice(new BigDecimal("50.00"));

        ProductEntity product = new ProductEntity();
        product.setId(1);
        product.setClientId(1);
        product.setMrp(new BigDecimal("60.00"));
        product.setProductName("Product 1");

        InventoryEntity inventory = new InventoryEntity();
        inventory.setProductId(1);
        inventory.setQuantity(5); // Less than required

        when(productApi.getByIds(List.of(1))).thenReturn(List.of(product));
        when(clientApi.isClientEnabled(1)).thenReturn(true);
        
        OrderEntity order = new OrderEntity();
        order.setId(1);
        when(orderApi.create(any(OrderEntity.class))).thenReturn(order);
        
        when(inventoryFlow.getInventoriesByProductIds(any())).thenReturn(List.of(inventory));

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> orderFlow.createOrder(List.of(item)));
        assertEquals(ApiStatus.CONFLICT, exception.getStatus());
        assertEquals("Insufficient inventory for product: Product 1", exception.getMessage());
        verify(productApi, times(1)).getByIds(List.of(1));
        verify(clientApi).isClientEnabled(1);
        verify(orderApi).create(any(OrderEntity.class));
        verifyNoInteractions(orderItemApi);
    }

    @Test
    void should_throw_exception_when_creating_order_with_inventory_not_found() {
        // Arrange
        OrderItemEntity item = new OrderItemEntity();
        item.setProductId(1);
        item.setQuantity(1);
        item.setSellingPrice(new BigDecimal("50.00"));

        ProductEntity product = new ProductEntity();
        product.setId(1);
        product.setClientId(1);
        product.setMrp(new BigDecimal("60.00"));
        product.setProductName("Product 1");

        when(productApi.getByIds(List.of(1))).thenReturn(List.of(product));
        when(clientApi.isClientEnabled(1)).thenReturn(true);
        when(inventoryFlow.getInventoriesByProductIds(any())).thenThrow(new ApiException(ApiStatus.NOT_FOUND, "Inventory not found for one or more products"));

        OrderEntity order = new OrderEntity();
        order.setId(1);
        when(orderApi.create(any(OrderEntity.class))).thenReturn(order);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> orderFlow.createOrder(List.of(item)));
        assertEquals(ApiStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Inventory not found for one or more products", exception.getMessage());
        verify(productApi, times(1)).getByIds(List.of(1));
        verify(clientApi).isClientEnabled(1);
        verify(inventoryFlow).getInventoriesByProductIds(any());
        verify(orderApi).create(any(OrderEntity.class));
        verifyNoInteractions(orderItemApi);
    }

    @Test
    void should_get_order_by_id_when_valid_input() {
        // Arrange
        OrderEntity order = new OrderEntity();
        order.setId(1);

        when(orderApi.getById(1)).thenReturn(order);

        // Act
        OrderEntity result = orderFlow.getById(1);

        // Assert
        assertEquals(1, result.getId());
        verify(orderApi).getById(1);
    }

    @Test
    void should_throw_exception_when_getting_order_by_null_id() {
        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> orderFlow.getById(null));
        assertEquals(ApiStatus.BAD_DATA, exception.getStatus());
        assertEquals("Order ID is required", exception.getMessage());
        verifyNoInteractions(orderApi);
    }

    @Test
    void should_search_orders() {
        // Arrange
        OrderStatus status = OrderStatus.CREATED;
        Integer clientId = 1;
        ZonedDateTime start = ZonedDateTime.now().minusDays(1);
        ZonedDateTime end = ZonedDateTime.now().plusDays(1);
        int page = 0;
        int pageSize = 10;

        List<OrderEntity> orders = List.of(new OrderEntity(), new OrderEntity());
        Page<OrderEntity> orderPage = new PageImpl<>(orders);

        when(orderApi.search(status, clientId, start, end, page, pageSize)).thenReturn(orderPage);

        // Act
        Page<OrderEntity> result = orderFlow.searchOrders(status, clientId, start, end, page, pageSize);

        // Assert
        assertEquals(2, result.getContent().size());
        verify(orderApi).search(status, clientId, start, end, page, pageSize);
    }

    @Test
    void should_cancel_order_when_valid_input() {
        // Arrange
        OrderEntity order = new OrderEntity();
        order.setId(1);
        order.setStatus(OrderStatus.CREATED);

        OrderItemEntity item = new OrderItemEntity();
        item.setProductId(1);
        item.setQuantity(2);

        InventoryEntity inventory = new InventoryEntity();
        inventory.setProductId(1);
        inventory.setQuantity(5);

        when(orderApi.getById(1)).thenReturn(order);
        when(orderItemApi.getByOrderId(1)).thenReturn(List.of(item));
        when(orderApi.update(order)).thenReturn(order);
        
        when(inventoryFlow.getInventoriesByProductIds(any())).thenReturn(List.of(inventory));
        
        ProductEntity product = new ProductEntity();
        product.setId(1);
        product.setProductName("Test Product");
        product.setClientId(1);
        when(productApi.getByIds(List.of(1))).thenReturn(List.of(product));

        // Act
        OrderEntity result = orderFlow.cancelOrder(1);

        // Assert
        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        assertEquals(7, inventory.getQuantity()); // 5 + 2 restored
        verify(orderApi).getById(1);
        verify(orderItemApi).getByOrderId(1);
        verify(inventoryFlow).bulkUpsert(anyList());
        verify(orderApi).update(order);
    }

    @Test
    void should_throw_exception_when_cancelling_order_not_found() {
        // Arrange
        when(orderApi.getById(1)).thenThrow(new ApiException(ApiStatus.NOT_FOUND, "Order not found"));

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> orderFlow.cancelOrder(1));
        assertEquals(ApiStatus.NOT_FOUND, exception.getStatus());
        verify(orderApi).getById(1);
        verifyNoInteractions(orderItemApi, inventoryApi);
    }

    @Test
    void should_throw_exception_when_cancelling_order_not_in_created_status() {
        // Arrange
        OrderEntity order = new OrderEntity();
        order.setId(1);
        order.setStatus(OrderStatus.INVOICED);

        when(orderApi.getById(1)).thenReturn(order);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> orderFlow.cancelOrder(1));
        assertEquals(ApiStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("INVOICED order cannot be cancelled", exception.getMessage());
        verify(orderApi).getById(1);
        verifyNoInteractions(orderItemApi, inventoryApi);
    }

    @Test
    void should_throw_exception_when_cancelling_order_with_inventory_not_found() {
        // Arrange
        OrderEntity order = new OrderEntity();
        order.setId(1);
        order.setStatus(OrderStatus.CREATED);

        OrderItemEntity item = new OrderItemEntity();
        item.setProductId(1);
        item.setQuantity(2);

        when(orderApi.getById(1)).thenReturn(order);
        when(orderItemApi.getByOrderId(1)).thenReturn(List.of(item));
        
        when(inventoryFlow.getInventoriesByProductIds(any())).thenThrow(
            new ApiException(ApiStatus.NOT_FOUND, "Inventory not found for product: Product 1", "productId", "Inventory not found for product: Product 1")
        );

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> orderFlow.cancelOrder(1));
        assertEquals(ApiStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Inventory not found for product: Product 1", exception.getMessage());
        verify(orderApi).getById(1);
        verify(orderItemApi).getByOrderId(1);
        verify(orderApi, never()).update(any(OrderEntity.class));
    }

    @Test
    void should_handle_multiple_items_in_order_creation() {
        // Arrange
        OrderItemEntity item1 = new OrderItemEntity();
        item1.setProductId(1);
        item1.setQuantity(2);
        item1.setSellingPrice(new BigDecimal("50.00"));

        OrderItemEntity item2 = new OrderItemEntity();
        item2.setProductId(2);
        item2.setQuantity(3);
        item2.setSellingPrice(new BigDecimal("30.00"));

        List<OrderItemEntity> items = List.of(item1, item2);

        ProductEntity product1 = new ProductEntity();
        product1.setId(1);
        product1.setClientId(1);
        product1.setMrp(new BigDecimal("60.00"));
        product1.setProductName("Product 1");

        ProductEntity product2 = new ProductEntity();
        product2.setId(2);
        product2.setClientId(1);
        product2.setMrp(new BigDecimal("40.00"));
        product2.setProductName("Product 2");

        InventoryEntity inventory1 = new InventoryEntity();
        inventory1.setProductId(1);
        inventory1.setQuantity(10);

        InventoryEntity inventory2 = new InventoryEntity();
        inventory2.setProductId(2);
        inventory2.setQuantity(5);

        OrderEntity order = new OrderEntity();
        order.setId(1);

        when(productApi.getByIds(List.of(1, 2))).thenReturn(List.of(product1, product2));
        when(clientApi.isClientEnabled(1)).thenReturn(true);
        when(orderApi.create(any(OrderEntity.class))).thenReturn(order);
        
        when(inventoryFlow.getInventoriesByProductIds(any())).thenReturn(List.of(inventory1, inventory2));

        // Act
        OrderEntity result = orderFlow.createOrder(items);

        // Assert
        assertEquals(1, result.getId());
        assertEquals(8, inventory1.getQuantity()); // 10 - 2
        assertEquals(2, inventory2.getQuantity()); // 5 - 3
        verify(productApi, times(1)).getByIds(List.of(1, 2));
        verify(clientApi).isClientEnabled(1);
        verify(orderApi).create(any(OrderEntity.class));
        verify(orderItemApi).createAll(anyList());
        verify(inventoryFlow).bulkUpsert(anyList());
    }

    @Test
    void should_handle_duplicate_product_ids_in_order_items() {
        // Arrange
        OrderItemEntity item1 = new OrderItemEntity();
        item1.setProductId(1);
        item1.setQuantity(2);
        item1.setSellingPrice(new BigDecimal("50.00"));

        OrderItemEntity item2 = new OrderItemEntity();
        item2.setProductId(1); // Same product
        item2.setQuantity(1);
        item2.setSellingPrice(new BigDecimal("50.00"));

        List<OrderItemEntity> items = List.of(item1, item2);

        ProductEntity product = new ProductEntity();
        product.setId(1);
        product.setClientId(1);
        product.setMrp(new BigDecimal("60.00"));
        product.setProductName("Product 1");

        InventoryEntity inventory = new InventoryEntity();
        inventory.setProductId(1);
        inventory.setQuantity(10);

        OrderEntity order = new OrderEntity();
        order.setId(1);

        when(productApi.getByIds(List.of(1))).thenReturn(List.of(product));
        when(clientApi.isClientEnabled(1)).thenReturn(true);
        when(orderApi.create(any(OrderEntity.class))).thenReturn(order);
        
        when(inventoryFlow.getInventoriesByProductIds(any())).thenReturn(List.of(inventory));

        // Act
        OrderEntity result = orderFlow.createOrder(items);

        // Assert
        assertEquals(1, result.getId());
        assertEquals(7, inventory.getQuantity()); // 10 - (2 + 1)
        verify(productApi, times(1)).getByIds(List.of(1)); // Should be called with distinct product IDs
        verify(clientApi).isClientEnabled(1);
        verify(inventoryFlow).getInventoriesByProductIds(any());
        verify(inventoryFlow).bulkUpsert(anyList());
        verify(orderApi).create(any(OrderEntity.class));
        verify(orderItemApi).createAll(anyList());
    }
}
