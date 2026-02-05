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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.increff.pos.util.ConversionUtil.createOrderItem;

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

    @Autowired
    private InventoryFlow inventoryFlow;

    private void validateOrderItems(List<OrderItemEntity> items) {
        if (Objects.isNull(items) || items.isEmpty()) {
            throw new ApiException(ApiStatus.BAD_DATA, "Order must contain at least one item", "items", "Order must contain at least one item");
        }
        
        validateSellingPriceAgainstMrp(items);
    }
    
    private void validateSellingPriceAgainstMrp(List<OrderItemEntity> items) {
        List<Integer> productIds = items.stream()
                .map(OrderItemEntity::getProductId)
                .distinct()
                .toList();
        
        List<ProductEntity> products = productApi.getByIds(productIds);
        Map<Integer, ProductEntity> productMap = products.stream()
                .collect(Collectors.toMap(ProductEntity::getId, product -> product));
        
        for (OrderItemEntity item : items) {
            ProductEntity product = productMap.get(item.getProductId());
            if (Objects.isNull(product)) {
                throw new ApiException(ApiStatus.NOT_FOUND, "Product not found: " + item.getProductId(), "productId", "Product not found: " + item.getProductId());
            }
            
            if (item.getSellingPrice().compareTo(product.getMrp()) > 0) {
                throw new ApiException(
                    ApiStatus.BAD_DATA,
                    "Selling price cannot be greater than MRP for product: " + product.getProductName() + 
                    " (Selling Price: " + item.getSellingPrice() + ", MRP: " + product.getMrp() + ")",
                    "sellingPrice",
                    "Selling price cannot be greater than MRP"
                );
            }
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

        Map<Integer, String> productNameMap = getProductNameMap(productIds);

        List<InventoryEntity> inventories = inventoryFlow.validateAndGetInventories(items);
        Map<Integer, InventoryEntity> inventoryMap = inventories.stream()
                .collect(Collectors.toMap(InventoryEntity::getProductId, i -> i));

        List<InventoryEntity> updatedInventories = new ArrayList<>();

        for (OrderItemEntity item : items) {
            InventoryEntity inventory = inventoryMap.get(item.getProductId());

            String productName = productNameMap.get(item.getProductId());

            if (Objects.isNull(inventory)) {
                throw new ApiException(
                        ApiStatus.NOT_FOUND,
                        "Inventory not found for product: " + productName,
                        "productId",
                        "Inventory not found for product: " + productName
                );
            }

            if (inventory.getQuantity() < item.getQuantity()) {
                throw new ApiException(
                        ApiStatus.CONFLICT,
                        "Insufficient inventory for product: " + productName,
                        "quantity",
                        "Insufficient inventory for product: " + productName
                );
            }

            inventory.setQuantity(inventory.getQuantity() - item.getQuantity());
            updatedInventories.add(inventory);
        }

        inventoryApi.bulkUpsert(updatedInventories);
    }

    private void validateOrderTotalGreaterThanZero(List<OrderItemEntity> items) {
        BigDecimal orderTotal = calculateOrderTotal(items);
        if (orderTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(
                ApiStatus.BAD_DATA,
                "Order total must be greater than 0",
                "orderTotal",
                "Order total must be greater than 0"
            );
        }
    }

    private BigDecimal calculateOrderTotal(List<OrderItemEntity> items) {
        return items.stream()
                .map(item -> item.getSellingPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<Integer, String> getProductNameMap(List<Integer> productIds) {
        return productApi.getByIds(productIds).stream()
                .collect(Collectors.toMap(
                        ProductEntity::getId,
                        ProductEntity::getProductName
                ));
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
        
        List<InventoryEntity> inventories = inventoryFlow.validateAndGetInventories(items);
        Map<Integer, InventoryEntity> inventoryMap = inventories.stream()
                .collect(Collectors.toMap(InventoryEntity::getProductId, inventory -> inventory));

        Map<Integer, String> productNameMap = getProductNameMap(productIds);

        List<InventoryEntity> updatedInventories = new ArrayList<>();
        
        for (OrderItemEntity item : items) {
            InventoryEntity inventory = inventoryMap.get(item.getProductId());
            if (Objects.isNull(inventory)) {
                throw new ApiException(
                    ApiStatus.NOT_FOUND,
                    "Inventory not found for product: " + productNameMap.get(item.getProductId()),
                    "productId",
                    "Inventory not found for product: " + productNameMap.get(item.getProductId())
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

    private List<OrderItemEntity> aggregateOrderItems(List<OrderItemEntity> items) {

        Map<Integer, OrderItemEntity> aggregated = items.stream()
                .collect(Collectors.toMap(
                        OrderItemEntity::getProductId,
                        item -> {
                            OrderItemEntity copy = new OrderItemEntity();
                            copy.setProductId(item.getProductId());
                            copy.setQuantity(item.getQuantity());
                            copy.setSellingPrice(item.getSellingPrice());
                            return copy;
                        },
                        (existing, incoming) -> {
                            existing.setQuantity(existing.getQuantity() + incoming.getQuantity());
                            return existing;
                        }
                ));

        return new ArrayList<>(aggregated.values());
    }

    public OrderEntity createOrder(List<OrderItemEntity> items) {
        validateOrderItems(items);
        List<OrderItemEntity> aggregatedItems = aggregateOrderItems(items);
        validateOrderTotalGreaterThanZero(aggregatedItems);

        Integer clientId = validateAndGetClientId(aggregatedItems);
        validateClientEnabled(clientId);

        OrderEntity savedOrder = createOrderEntity(clientId);
        List<OrderItemEntity> persistedItems = processOrderItems(aggregatedItems, savedOrder.getId());

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
}
