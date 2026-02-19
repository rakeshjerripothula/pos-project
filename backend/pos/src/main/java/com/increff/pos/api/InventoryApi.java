package com.increff.pos.api;

import com.increff.pos.dao.InventoryDao;
import com.increff.pos.entity.InventoryEntity;
import com.increff.pos.entity.OrderItemEntity;
import com.increff.pos.entity.ProductEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventoryApi {

    @Autowired
    private InventoryDao inventoryDao;

    public List<InventoryEntity> getAllForEnabledClients() {
        return inventoryDao.selectAllForEnabledClients();
    }

    public List<InventoryEntity> getCheckByProductIds(List<Integer> productIds) {
        List<InventoryEntity> inventories = inventoryDao.selectByProductIds(productIds);
        if (inventories.size() != productIds.size())
            throw new ApiException(ApiStatus.NOT_FOUND, "Inventory not found for one or more products", "productId",
                    "Inventory not found for one or more products");
        return inventories;
    }

    public Page<InventoryEntity> getPagedForEnabledClients(String barcode, String productName, Pageable pageable) {
        return inventoryDao.selectPagedForEnabledClients(barcode, productName, pageable);
    }

    public InventoryEntity upsert(InventoryEntity input) {
        validateQuantity(input.getQuantity());
        InventoryEntity entity = inventoryDao.selectByProductId(input.getProductId())
                .orElseGet(() -> createNew(input.getProductId()));
        entity.setQuantity(input.getQuantity());
        return inventoryDao.save(entity);
    }

    public List<InventoryEntity> validateAndUpdateInventory(List<OrderItemEntity> items, Map<Integer, ProductEntity> productMap) {

        List<Integer> productIds = items.stream().map(OrderItemEntity::getProductId).distinct().toList();
        List<InventoryEntity> inventories = getCheckByProductIds(productIds);

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

        return bulkUpsert(updated);
    }

    public List<InventoryEntity> restoreInventory(List<OrderItemEntity> items) {
        List<Integer> productIds = items.stream().map(OrderItemEntity::getProductId).distinct().toList();

        List<InventoryEntity> inventories = getCheckByProductIds(productIds);
        Map<Integer, InventoryEntity> inventoryMap = inventories.stream()
                .collect(Collectors.toMap(InventoryEntity::getProductId, inventory -> inventory));

        List<InventoryEntity> updatedInventories = new ArrayList<>();
        
        for (OrderItemEntity item : items) {
            InventoryEntity inventory = inventoryMap.get(item.getProductId());
            int availableQty = inventory.getQuantity();
            inventory.setQuantity(availableQty + item.getQuantity());
            updatedInventories.add(inventory);
        }

        return bulkUpsert(updatedInventories);
    }

    public List<InventoryEntity> bulkUpsert(List<InventoryEntity> updated) {
        return inventoryDao.saveAll(updated);
    }

    private InventoryEntity createNew(Integer productId) {
        InventoryEntity entity = new InventoryEntity();
        entity.setProductId(productId);
        return entity;
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity < 0){
            throw new ApiException(ApiStatus.BAD_REQUEST, "Quantity cannot be negative", "quantity",
                    "Quantity cannot be negative");
        }
    }
}
