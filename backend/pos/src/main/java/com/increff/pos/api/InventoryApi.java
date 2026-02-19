package com.increff.pos.api;

import com.increff.pos.dao.InventoryDao;
import com.increff.pos.entity.InventoryEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryApi {

    @Autowired
    private InventoryDao inventoryDao;

    public List<InventoryEntity> getAllForEnabledClients() {
        return inventoryDao.selectAllForEnabledClients();
    }

    public List<InventoryEntity> getByProductIds(List<Integer> productIds) {
        return inventoryDao.selectByProductIds(productIds);
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

    private InventoryEntity createNew(Integer productId) {
        InventoryEntity entity = new InventoryEntity();
        entity.setProductId(productId);
        return entity;
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity < 0)
            throw new ApiException(ApiStatus.BAD_REQUEST, "Quantity cannot be negative", "quantity", "Quantity cannot be negative");
    }
}
