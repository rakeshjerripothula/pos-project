package com.increff.pos.api;

import com.increff.pos.dao.InventoryDao;
import com.increff.pos.entity.InventoryEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class InventoryApi {

    @Autowired
    private InventoryDao inventoryDao;

    public InventoryEntity upsert(InventoryEntity inventoryEntity) {

        if (inventoryEntity.getQuantity() < 0) {
            throw new ApiException(
                    ApiStatus.BAD_DATA,
                    "Quantity cannot be negative",
                    "quantity",
                    "Quantity cannot be negative"
            );
        }

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

    public Page<InventoryEntity> listAll(Pageable pageable) {
        return inventoryDao.findAll(pageable);
    }

    public InventoryEntity getByProductId(Integer productId) {

        return inventoryDao.findByProductId(productId)
                .orElseThrow(() -> new ApiException(
                        ApiStatus.NOT_FOUND,
                        "Inventory not found for productId: " + productId
                ));
    }


    public List<InventoryEntity> getByProductIds(List<Integer> productIds) {
        return inventoryDao.findByProductIds(productIds);
    }

    public Page<InventoryEntity> listForEnabledClients(Pageable pageable) {
        return inventoryDao.findForEnabledClients(pageable);
    }

    public List<InventoryEntity> bulkUpsert(List<InventoryEntity> inventoryEntities) {
        for (InventoryEntity inventory : inventoryEntities) {
            if (inventory.getQuantity() < 0) {
                throw new ApiException(
                        ApiStatus.BAD_DATA,
                        "Quantity cannot be negative",
                        "quantity",
                        "Quantity cannot be negative"
                );
            }
        }
        return inventoryDao.saveAll(inventoryEntities);
    }

}
