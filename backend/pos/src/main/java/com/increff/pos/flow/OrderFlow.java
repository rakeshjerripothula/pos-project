package com.increff.pos.flow;

import com.increff.pos.api.*;
import com.increff.pos.entity.*;
import com.increff.pos.domain.OrderStatus;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import com.increff.pos.model.data.OrderItemData;
import com.increff.pos.model.form.InvoiceForm;
import com.increff.pos.util.ConversionUtil;
import com.increff.pos.util.InvoiceConverter;
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
    private ProductApi productApi;

    @Autowired
    private ClientApi clientApi;

    @Autowired
    private InventoryFlow inventoryFlow;

    @Autowired
    private InvoiceApi invoiceApi;

    public Page<OrderEntity> searchOrders(OrderStatus status, Integer clientId, ZonedDateTime start, ZonedDateTime end,
            Integer page, Integer pageSize) {
        return orderApi.search(status, clientId, start, end, page, pageSize);
    }

    public List<OrderItemData> getOrderItems(Integer orderId) {

        List<OrderItemEntity> items = orderItemApi.getByOrderId(orderId);

        List<Integer> productIds = items.stream().map(OrderItemEntity::getProductId).distinct().toList();

        List<ProductEntity> products = productApi.getByIds(productIds);

        Map<Integer, ProductEntity> productMap = products.stream()
                .collect(Collectors.toMap(ProductEntity::getId, p -> p));

        return ConversionUtil.orderItemEntitiesToData(items, productMap);
    }

    public OrderEntity createOrder(List<OrderItemEntity> items) {
        validateOrderItems(items);
        List<OrderItemEntity> aggregatedItems = aggregateOrderItems(items);
        Map<Integer, ProductEntity> productMap = getProductMap(aggregatedItems);

        validateSellingPriceAgainstMrp(items, productMap);
        validateOrderTotalGreaterThanZero(aggregatedItems);

        Integer clientId = validateAndGetClientId(productMap);
        validateClientEnabled(clientId);

        OrderEntity savedOrder = createOrderEntity(clientId);
        List<OrderItemEntity> persistedItems = processOrderItems(aggregatedItems, savedOrder.getId(), productMap);

        orderItemApi.createAll(persistedItems);

        return savedOrder;
    }

    @Transactional(readOnly = true)
    public byte[] downloadInvoice(Integer orderId) {

        orderApi.getById(orderId);

        return invoiceApi.download(orderId);
    }

    private void validateOrderItems(List<OrderItemEntity> items) {
        if (Objects.isNull(items) || items.isEmpty()) {
            throw new ApiException(ApiStatus.BAD_DATA,
                    "Order must contain at least one item", "items", "Order must contain at least one item");
        }
    }

    private void validateSellingPriceAgainstMrp(List<OrderItemEntity> items, Map<Integer, ProductEntity> productMap) {

        for (OrderItemEntity item : items) {
            ProductEntity product = productMap.get(item.getProductId());
            if (item.getSellingPrice().compareTo(product.getMrp()) > 0) {
                throw new ApiException(
                        ApiStatus.BAD_DATA,
                        "Selling price cannot be greater than MRP for product: " + product.getProductName(),
                        "sellingPrice", "Selling price cannot be greater than MRP"
                );
            }
        }
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

    private void validateOrderTotalGreaterThanZero(List<OrderItemEntity> items) {
        BigDecimal orderTotal = calculateOrderTotal(items);
        if (orderTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(
                    ApiStatus.BAD_DATA, "Order total must be greater than 0",
                    "orderTotal", "Order total must be greater than 0"
            );
        }
    }

    private BigDecimal calculateOrderTotal(List<OrderItemEntity> items) {
        return items.stream().map(item -> item.getSellingPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public OrderEntity update(OrderEntity order) {
        return orderApi.update(order);
    }

    private Integer validateAndGetClientId(Map<Integer, ProductEntity> productMap) {
        Integer clientId = null;
        for (ProductEntity product : productMap.values()) {
            if (clientId == null) {
                clientId = product.getClientId();
            } else if (!clientId.equals(product.getClientId())) {
                throw new ApiException(
                        ApiStatus.BAD_DATA, "All products in an order must belong to the same client",
                        "clientId", "All products in an order must belong to the same client"
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

    private List<OrderItemEntity> processOrderItems(List<OrderItemEntity> items, Integer orderId,
            Map<Integer, ProductEntity> productMap) {

        validateAndUpdateInventory(items, productMap);

        return items.stream().map(item -> createOrderItem(item, orderId)).toList();
    }


    private void validateAndUpdateInventory(List<OrderItemEntity> items, Map<Integer, ProductEntity> productMap) {

        List<InventoryEntity> inventories = inventoryFlow.getInventoriesByProductIds(items);

        Map<Integer, InventoryEntity> inventoryMap = inventories.stream()
                .collect(Collectors.toMap(InventoryEntity::getProductId, i -> i));

        List<InventoryEntity> updated = new ArrayList<>();

        for (OrderItemEntity item : items) {

            InventoryEntity inventory = inventoryMap.get(item.getProductId());

            ProductEntity product = productMap.get(item.getProductId());

            if (inventory.getQuantity() < item.getQuantity()) {
                throw new ApiException(
                        ApiStatus.CONFLICT, "Insufficient inventory for product: " + product.getProductName(),
                        "quantity", "Insufficient inventory"
                );
            }

            inventory.setQuantity(inventory.getQuantity() - item.getQuantity());

            updated.add(inventory);
        }

        inventoryFlow.bulkUpsert(updated);
    }

    public OrderEntity cancelOrder(Integer orderId) {

        OrderEntity order = orderApi.getById(orderId);

        if (!order.getStatus().equals(OrderStatus.CREATED)) {
            throw new ApiException(
                ApiStatus.BAD_REQUEST, "INVOICED order cannot be cancelled", "status",
                "INVOICED order cannot be cancelled"
            );
        }

        List<OrderItemEntity> items = orderItemApi.getByOrderId(orderId);
        
        List<InventoryEntity> inventories = inventoryFlow.getInventoriesByProductIds(items);
        Map<Integer, InventoryEntity> inventoryMap = inventories.stream()
                .collect(Collectors.toMap(InventoryEntity::getProductId, inventory -> inventory));

        Map<Integer, ProductEntity> productMap = getProductMap(items);

        List<InventoryEntity> updatedInventories = new ArrayList<>();
        
        for (OrderItemEntity item : items) {
            InventoryEntity inventory = inventoryMap.get(item.getProductId());
            if (Objects.isNull(inventory)) {
                throw new ApiException(
                    ApiStatus.NOT_FOUND, "Inventory not found for product: " + productMap.get(item.getProductId()),
                    "productId", "Inventory not found for product: " + productMap.get(item.getProductId())
                );
            }
            
            int availableQty = inventory.getQuantity();
            inventory.setQuantity(availableQty + item.getQuantity());
            updatedInventories.add(inventory);
        }

        inventoryFlow.bulkUpsert(updatedInventories);

        order.setStatus(OrderStatus.CANCELLED);
        return orderApi.update(order);
    }

    public OrderEntity getById(Integer orderId) {
        if (Objects.isNull(orderId)) {
            throw new ApiException(ApiStatus.BAD_DATA, "Order ID is required", "orderId", "Order ID is required");
        }
        return orderApi.getById(orderId);
    }

    @Transactional(readOnly = true)
    public InvoiceForm buildInvoiceForm(Integer orderId) {

        OrderEntity order = orderApi.getById(orderId);

        if (!order.getStatus().equals(OrderStatus.CREATED)) {
            throw new ApiException(ApiStatus.BAD_REQUEST, "Only CREATED orders can be invoiced");
        }

        List<OrderItemEntity> items = orderItemApi.getByOrderId(orderId);

        if (items.isEmpty()) {
            throw new ApiException(ApiStatus.BAD_DATA, "Order has no items");
        }

        Map<Integer, ProductEntity> productMap = getProductMap(items);

        ClientEntity client = clientApi.getById(order.getClientId());

        return InvoiceConverter.convert(order, items, productMap, client.getClientName());
    }

    public InvoiceEntity saveInvoice(Integer orderId, String filePath) {

        if (invoiceApi.existsForOrder(orderId)) {
            throw new ApiException(
                    ApiStatus.BAD_REQUEST,
                    "Invoice already exists for order " + orderId
            );
        }

        OrderEntity order = orderApi.getById(orderId);

        if (!order.getStatus().equals(OrderStatus.CREATED)) {
            throw new ApiException(
                    ApiStatus.BAD_REQUEST,
                    "Invoice can only be generated for CREATED orders"
            );
        }

        InvoiceEntity invoice = new InvoiceEntity();
        invoice.setOrderId(orderId);
        invoice.setFilePath(filePath);

        invoiceApi.create(invoice);

        order.setStatus(OrderStatus.INVOICED);
        orderApi.update(order);

        return invoice;
    }


    private Map<Integer, ProductEntity> getProductMap(List<OrderItemEntity> items) {

        List<Integer> productIds = items.stream().map(OrderItemEntity::getProductId).distinct().toList();

        List<ProductEntity> products = productApi.getByIds(productIds);

        if (products.size() != productIds.size()) {
            throw new ApiException(
                    ApiStatus.NOT_FOUND, "One or more products not found", "productId", "One or more products not found"
            );
        }

        return products.stream().collect(Collectors.toMap(ProductEntity::getId, p -> p));
    }

}
