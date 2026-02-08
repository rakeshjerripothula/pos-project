package com.increff.pos.flow;

import com.increff.pos.api.ClientApi;
import com.increff.pos.api.InventoryApi;
import com.increff.pos.api.ProductApi;
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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
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

    public List<InventoryEntity> validateAndGetInventories(List<OrderItemEntity> items) {

        List<Integer> productIds = items.stream()
                .map(OrderItemEntity::getProductId)
                .distinct()
                .toList();

        List<InventoryEntity> inventories = inventoryApi.getByProductIds(productIds);

        if (inventories.size() != productIds.size()) {
            throw new ApiException(
                    ApiStatus.NOT_FOUND,
                    "Inventory not found for one or more products",
                    "productId",
                    "Inventory not found for one or more products"
            );
        }

        List<ProductEntity> products = productApi.getByIds(productIds);

        if (products.size() != productIds.size()) {
            throw new ApiException(
                    ApiStatus.NOT_FOUND,
                    "One or more products not found",
                    "productId",
                    "One or more products not found"
            );
        }

        Integer clientId = products.getFirst().getClientId();
        for (ProductEntity product : products) {
            if (!clientId.equals(product.getClientId())) {
                throw new ApiException(
                        ApiStatus.BAD_DATA,
                        "All products in an order must belong to the same client",
                        "clientId",
                        "All products in an order must belong to the same client"
                );
            }
        }

        if (!clientApi.isClientEnabled(clientId)) {
            throw new ApiException(
                    ApiStatus.FORBIDDEN,
                    "Client is disabled",
                    "clientId",
                    "Client is disabled"
            );
        }

        return inventories;
    }


    public InventoryEntity getByProductId(Integer productId) {

        InventoryEntity inventory = inventoryApi.getByProductId(productId);

        ProductEntity product = productApi.getProductById(productId);

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

    private Map<Integer, ProductEntity> getProductIds(List<InventoryEntity> allInventories) {
        List<Integer> productIds = allInventories.stream()
                .map(InventoryEntity::getProductId)
                .distinct()
                .toList();

        List<ProductEntity> products = productApi.getByIds(productIds);
        return products.stream().collect(Collectors.toMap(ProductEntity::getId, product -> product));
    }

    public Page<InventoryEntity> listForEnabledClients(Pageable pageable) {
        return inventoryApi.listForEnabledClients(pageable);
    }

    public List<InventoryEntity> listAllForEnabledClients() {
        return inventoryApi.listAllForEnabledClients();
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
