package com.increff.pos.flow;

import com.increff.pos.api.ClientApi;
import com.increff.pos.api.InventoryApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.entity.InventoryEntity;
import com.increff.pos.entity.ProductEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class InventoryFlow {

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private ProductApi productApi;

    @Autowired
    private ClientApi clientApi;

    public InventoryEntity upsert(InventoryEntity inventory) {

        ProductEntity product =
                productApi.getProductById(inventory.getProductId());

        if (!clientApi.isClientEnabled(product.getClientId())) {
            throw new ApiException(
                    ApiStatus.FORBIDDEN,
                    "Client is disabled",
                    "clientId",
                    "Client is disabled"
            );
        }

        return inventoryApi.upsert(inventory);
    }

    public InventoryEntity getByProductId(Integer productId) {

        InventoryEntity inventory =
                inventoryApi.getByProductId(productId);

        ProductEntity product =
                productApi.getProductById(productId);

        if (!clientApi.isClientEnabled(product.getClientId())) {
            throw new ApiException(
                    ApiStatus.FORBIDDEN,
                    "Client is disabled",
                    "clientId",
                    "Client is disabled"
            );
        }

        return inventory;
    }

    public List<InventoryEntity> listForEnabledClients() {

        List<InventoryEntity> allInventories = inventoryApi.listAll();

        Map<Integer, ProductEntity> productMap = getProductIds(allInventories);

        return allInventories.stream()
                .filter(inv -> {
                    ProductEntity product = productMap.get(inv.getProductId());
                    return !Objects.isNull(product) && clientApi.isClientEnabled(product.getClientId());
                })
                .toList();
    }

    private Map<Integer, ProductEntity> getProductIds(List<InventoryEntity> allInventories) {
        List<Integer> productIds = allInventories.stream()
                .map(InventoryEntity::getProductId)
                .distinct()
                .toList();

        List<ProductEntity> products = productApi.getByIds(productIds);
        return products.stream().collect(Collectors.toMap(ProductEntity::getId, product -> product));
    }

    public List<InventoryEntity> bulkUpsert(List<InventoryEntity> inventories) {
        Map<Integer, ProductEntity> productMap = getProductIds(inventories);

        for (InventoryEntity inventory : inventories) {
            ProductEntity product = productMap.get(inventory.getProductId());
            if (Objects.isNull(product)) {
                throw new ApiException(
                    ApiStatus.NOT_FOUND,
                    "Product not found: " + inventory.getProductId(),
                    "productId",
                    "Product not found: " + inventory.getProductId()
                );
            }

            if (!clientApi.isClientEnabled(product.getClientId())) {
                throw new ApiException(
                        ApiStatus.FORBIDDEN,
                        "Client is disabled",
                        "clientId",
                        "Client is disabled"
                );
            }
        }

        return inventoryApi.bulkUpsert(inventories);
    }
}
