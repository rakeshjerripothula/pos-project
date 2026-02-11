package com.increff.pos.api;

import com.increff.pos.dao.InventoryDao;
import com.increff.pos.entity.InventoryEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InventoryApi {

    @Autowired
    private InventoryDao inventoryDao;

    public List<InventoryEntity> getAllForEnabledClients() {
        return inventoryDao.findAllForEnabledClients();
    }

    public InventoryEntity upsertInventory(InventoryEntity inventoryEntity) {

        Integer productId = inventoryEntity.getProductId();

        InventoryEntity inventory = inventoryDao.findByProductId(productId)
                .orElseGet(() -> {
                    InventoryEntity e = new InventoryEntity();
                    e.setProductId(productId);
                    return e;
                });

        inventory.setQuantity(inventoryEntity.getQuantity());
        return inventoryDao.save(inventory);
    }

    public Page<InventoryEntity> searchForEnabledClients(String barcode, String productName, Pageable pageable) {
        return inventoryDao.searchForEnabledClients(barcode, productName, pageable);
    }

    public List<InventoryEntity> bulkUpsert(List<InventoryEntity> inputs) {

        for (InventoryEntity input : inputs) {
            if (input.getQuantity() < 0) {
                throw new ApiException(
                        ApiStatus.BAD_DATA, "Quantity cannot be negative",
                        "quantity", "Quantity cannot be negative"
                );
            }
        }

        List<Integer> productIds = inputs.stream().map(InventoryEntity::getProductId).toList();

        Map<Integer, InventoryEntity> existingMap = inventoryDao.findByProductIds(productIds).stream()
                .collect(Collectors.toMap(InventoryEntity::getProductId, i -> i));

        List<InventoryEntity> toSave = new ArrayList<>();

        for (InventoryEntity input : inputs) {

            InventoryEntity existing = existingMap.get(input.getProductId());

            if (existing != null) {
                existing.setQuantity(input.getQuantity());
                toSave.add(existing);
            } else {
                toSave.add(input);
            }
        }

        return inventoryDao.saveAll(toSave);
    }

    public List<InventoryEntity> getByProductIds(List<Integer> productIds) {
        return inventoryDao.findByProductIds(productIds);
    }

}
