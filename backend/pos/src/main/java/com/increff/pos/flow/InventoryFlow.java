package com.increff.pos.flow;

import com.increff.pos.api.ClientApi;
import com.increff.pos.api.InventoryApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.entity.InventoryEntity;
import com.increff.pos.entity.OrderItemEntity;
import com.increff.pos.entity.ProductEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.data.PagedResponse;
import com.increff.pos.util.ConversionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
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

    public List<InventoryData> listAllForEnabledClientsWithData() {

        List<InventoryEntity> inventories = inventoryApi.getAllForEnabledClients();

        if (inventories.isEmpty()) {
            return List.of();
        }

        Map<Integer, ProductEntity> productMap = getProductMap(inventories);

        return inventories.stream().map(inv ->
                                ConversionUtil.inventoryEntityToData(inv, productMap.get(inv.getProductId()))).toList();
    }

    public InventoryData upsert(InventoryEntity inventory) {

        ProductEntity product = productApi.getProductById(inventory.getProductId());

        if (!clientApi.isClientEnabled(product.getClientId())) {
            throw new ApiException(ApiStatus.FORBIDDEN, "Client is disabled", "clientId", "Client is disabled");
        }

        InventoryEntity saved = inventoryApi.upsertInventory(inventory);

        return ConversionUtil.inventoryEntityToData(saved, product);
    }

    public PagedResponse<InventoryData> searchForEnabledClientsWithData(String barcode, String productName,
            Pageable pageable) {

        Page<InventoryEntity> page = inventoryApi.searchForEnabledClients(barcode, productName, pageable);

        if (page.isEmpty()) {
            return new PagedResponse<>(List.of(), 0L);
        }

        Map<Integer, ProductEntity> productMap = getProductMap(page.getContent());

        List<InventoryData> data = page.getContent().stream()
                .map(inv -> ConversionUtil.inventoryEntityToData(inv, productMap.get(inv.getProductId()))).toList();

        return new PagedResponse<>(data, page.getTotalElements());
    }

    public List<InventoryData> bulkUpsertAndGetData(List<InventoryEntity> inventories) {
        List<InventoryEntity> saved = bulkUpsert(inventories);

        Map<Integer, ProductEntity> productMap = getProductMap(saved);

        return saved.stream().map(inv -> ConversionUtil.inventoryEntityToData(inv, productMap.get(inv.getProductId())))
                .toList();
    }

    public List<InventoryEntity> bulkUpsert(List<InventoryEntity> inventories) {
        Map<Integer, ProductEntity> productMap = getProductMap(inventories);

        List<Integer> clientIds = productMap.values().stream().map(ProductEntity::getClientId).distinct().toList();

        List<Integer> disabledClientIds = clientApi.getDisabledClientIds(clientIds);

        for (InventoryEntity inventory : inventories) {
            ProductEntity product = productMap.get(inventory.getProductId());
            if (Objects.isNull(product)) {
                throw new ApiException(
                        ApiStatus.NOT_FOUND, "Product not found: " + inventory.getProductId(),
                        "productId", "Product not found: " + inventory.getProductId()
                );
            }

            if (disabledClientIds.contains(product.getClientId())) {
                throw new ApiException(ApiStatus.FORBIDDEN, "Client is disabled", "clientId", "Client is disabled");
            }
        }

        return inventoryApi.bulkUpsert(inventories);
    }

    public List<InventoryEntity> validateAndGetInventories(List<OrderItemEntity> items) {

        List<Integer> productIds = items.stream().map(OrderItemEntity::getProductId).distinct().toList();

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
                    ApiStatus.NOT_FOUND, "One or more products not found", "productId", "One or more products not found"
            );
        }

        Integer clientId = products.getFirst().getClientId();
        for (ProductEntity product : products) {
            if (!clientId.equals(product.getClientId())) {
                throw new ApiException(
                        ApiStatus.BAD_DATA, "All products in an order must belong to the same client",
                        "clientId", "All products in an order must belong to the same client"
                );
            }
        }

        if (!clientApi.isClientEnabled(clientId)) {
            throw new ApiException(ApiStatus.FORBIDDEN, "Client is disabled", "clientId", "Client is disabled");
        }

        return inventories;
    }

    private Map<Integer, ProductEntity> getProductMap(List<InventoryEntity> allInventories) {
        List<Integer> productIds = allInventories.stream().map(InventoryEntity::getProductId).distinct().toList();
        List<ProductEntity> products = productApi.getByIds(productIds);
        return products.stream().collect(Collectors.toMap(ProductEntity::getId, product -> product));
    }

}
