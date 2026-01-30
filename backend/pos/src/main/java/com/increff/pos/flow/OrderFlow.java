package com.increff.pos.flow;

import com.increff.pos.api.*;
import com.increff.pos.entity.InventoryEntity;
import com.increff.pos.entity.OrderEntity;
import com.increff.pos.entity.OrderItemEntity;
import com.increff.pos.domain.OrderStatus;
import com.increff.pos.entity.ProductEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderFlow {

    @Autowired
    private OrderApi orderApi;

    @Autowired
    private OrderItemApi orderItemApi;

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private ProductApi productApi;

    @Autowired
    private ClientApi clientApi;

    public OrderEntity createOrder(List<OrderItemEntity> items) {
        validateOrderItems(items);
        
        Integer clientId = validateAndGetClientId(items);
        validateClientEnabled(clientId);
        
        OrderEntity savedOrder = createOrderEntity(clientId);
        List<OrderItemEntity> persistedItems = processOrderItems(items, savedOrder.getId());

        orderItemApi.createAll(persistedItems);
        
        return savedOrder;
    }

    public OrderEntity getById(Integer orderId) {
        if (Objects.isNull(orderId)) {
            throw new ApiException(ApiStatus.BAD_DATA, "Order ID is required", "orderId", "Order ID is required");
        }
        return orderApi.getById(orderId);
    }

    public Page<OrderEntity> searchOrders(OrderStatus status, Integer clientId, ZonedDateTime start, ZonedDateTime end, int page, int pageSize) {
        return orderApi.search(status, clientId, start, end, page, pageSize);
    }

    private void validateOrderItems(List<OrderItemEntity> items) {
        if (Objects.isNull(items) || items.isEmpty()) {
            throw new ApiException(ApiStatus.BAD_DATA, "Order must contain at least one item", "items", "Order must contain at least one item");
        }
    }

    private Integer validateAndGetClientId(List<OrderItemEntity> items) {
        List<Integer> productIds = items.stream()
                .map(OrderItemEntity::getProductId)
                .distinct()
                .toList();
        
        List<ProductEntity> products = productApi.getByIds(productIds);
        
        if (products.size() != productIds.size()) {
            throw new ApiException(ApiStatus.NOT_FOUND, "One or more products not found", "products", "One or more products not found");
        }

        return getClientId(products);
    }

    private static @Nullable Integer getClientId(List<ProductEntity> products) {
        Integer clientId = null;
        for (ProductEntity product : products) {
            if (Objects.isNull(clientId)) {
                clientId = product.getClientId();
            } else if (!clientId.equals(product.getClientId())) {
                throw new ApiException(
                    ApiStatus.BAD_DATA,
                    "All products in an order must belong to the same client",
                    "clientId",
                    "All products in an order must belong to the same client"
                );
            }
        }
        return clientId;
    }

    private void validateClientEnabled(Integer clientId) {
        if (!clientApi.isClientEnabled(clientId)) {
            throw new ApiException(ApiStatus.FORBIDDEN, "Client is disabled", "clientId", "Client is disabled");
        }
    }

    private OrderEntity createOrderEntity(Integer clientId) {
        OrderEntity order = new OrderEntity();
        order.setClientId(clientId);
        order.setStatus(OrderStatus.CREATED);
        return orderApi.create(order);
    }

    private List<OrderItemEntity> processOrderItems(List<OrderItemEntity> items, Integer orderId) {
        List<OrderItemEntity> persistedItems = new ArrayList<>();
        
        validateAndUpdateInventory(items);
        
        for (OrderItemEntity item : items) {
            OrderItemEntity orderItem = createOrderItem(item, orderId);
            persistedItems.add(orderItem);
        }
        
        return persistedItems;
    }

    private void validateAndUpdateInventory(List<OrderItemEntity> items) {
        List<Integer> productIds = items.stream()
                .map(OrderItemEntity::getProductId)
                .distinct()
                .toList();
        
        List<InventoryEntity> inventories = inventoryApi.getByProductIds(productIds);

        Map<Integer, InventoryEntity> inventoryMap = inventories.stream()
                .collect(Collectors.toMap(InventoryEntity::getProductId, inventory -> inventory));
        
        List<InventoryEntity> updatedInventories = new ArrayList<>();
        
        for (OrderItemEntity item : items) {
            InventoryEntity inventory = inventoryMap.get(item.getProductId());
            if (Objects.isNull(inventory)) {
                throw new ApiException(
                    ApiStatus.NOT_FOUND,
                    "Inventory not found for product: " + item.getProductId(),
                    "productId",
                    "Inventory not found for product: " + item.getProductId()
                );
            }
            
            int availableQty = inventory.getQuantity();
            if (availableQty < item.getQuantity()) {
                throw new ApiException(
                    ApiStatus.CONFLICT,
                    "Insufficient inventory for product: " + item.getProductId(),
                    "quantity",
                    "Insufficient inventory for product: " + item.getProductId()
                );
            }
            
            inventory.setQuantity(availableQty - item.getQuantity());
            updatedInventories.add(inventory);
        }
        
        inventoryApi.bulkUpsert(updatedInventories);
    }

    private OrderItemEntity createOrderItem(OrderItemEntity item, Integer orderId) {
        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setOrderId(orderId);
        orderItem.setProductId(item.getProductId());
        orderItem.setQuantity(item.getQuantity());
        orderItem.setSellingPrice(item.getSellingPrice());
        return orderItem;
    }

    public OrderEntity cancelOrder(Integer orderId) {

        OrderEntity order = orderApi.getById(orderId);

        if (!order.getStatus().equals(OrderStatus.CREATED)) {
            throw new ApiException(
                ApiStatus.BAD_REQUEST,
                "Only CREATED orders can be cancelled",
                "status",
                "Only CREATED orders can be cancelled"
            );
        }

        List<OrderItemEntity> items = orderItemApi.getByOrderId(orderId);

        List<Integer> productIds = items.stream()
                .map(OrderItemEntity::getProductId)
                .distinct()
                .toList();
        
        List<InventoryEntity> inventories = inventoryApi.getByProductIds(productIds);
        Map<Integer, InventoryEntity> inventoryMap = inventories.stream()
                .collect(Collectors.toMap(InventoryEntity::getProductId, inventory -> inventory));
        
        List<InventoryEntity> updatedInventories = new ArrayList<>();
        
        for (OrderItemEntity item : items) {
            InventoryEntity inventory = inventoryMap.get(item.getProductId());
            if (Objects.isNull(inventory)) {
                throw new ApiException(
                    ApiStatus.NOT_FOUND,
                    "Inventory not found for product: " + item.getProductId(),
                    "productId",
                    "Inventory not found for product: " + item.getProductId()
                );
            }
            
            int availableQty = inventory.getQuantity();
            inventory.setQuantity(availableQty + item.getQuantity());
            updatedInventories.add(inventory);
        }

        inventoryApi.bulkUpsert(updatedInventories);

        order.setStatus(OrderStatus.CANCELLED);
        return orderApi.update(order);
    }
}
