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

    @Transactional
    public PagedResponse<InventoryData> searchForEnabledClientsWithData(
            String barcode,
            String productName,
            Pageable pageable
    ) {

        Page<InventoryEntity> page = inventoryApi.searchForEnabledClients(barcode, productName, pageable);

        if (page.isEmpty()) {
            return new PagedResponse<>(List.of(), 0L);
        }

        List<Integer> productIds = page.getContent().stream()
                .map(InventoryEntity::getProductId)
                .distinct()
                .toList();

        Map<Integer, ProductEntity> productMap =
                productApi.getByIds(productIds).stream()
                        .collect(Collectors.toMap(ProductEntity::getId, p -> p));

        List<InventoryData> data = page.getContent().stream()
                .map(inv -> ConversionUtil.inventoryEntityToData(
                        inv,
                        productMap.get(inv.getProductId())
                ))
                .toList();

        return new PagedResponse<>(data, page.getTotalElements());
    }

    public InventoryData upsertAndGetData(InventoryEntity inventory) {
        InventoryEntity saved = upsert(inventory);
        ProductEntity product = productApi.getProductById(inventory.getProductId());
        return ConversionUtil.inventoryEntityToData(saved, product);
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

    public InventoryData getByProductIdWithData(Integer productId) {
        InventoryEntity inventory = getByProductId(productId);
        ProductEntity product = productApi.getProductById(productId);
        return ConversionUtil.inventoryEntityToData(inventory, product);
    }

    private Map<Integer, ProductEntity> getProductIds(List<InventoryEntity> allInventories) {
        List<Integer> productIds = allInventories.stream()
                .map(InventoryEntity::getProductId)
                .distinct()
                .toList();

        List<ProductEntity> products = productApi.getByIds(productIds);
        return products.stream().collect(Collectors.toMap(ProductEntity::getId, product -> product));
    }

    public List<InventoryEntity> getByProductIds(List<Integer> productIds) {
        List<InventoryEntity> inventories = inventoryApi.getByProductIds(productIds);
        
        if (inventories.isEmpty()) {
            return inventories;
        }
        
        List<Integer> foundProductIds = inventories.stream().map(InventoryEntity::getProductId).toList();
        
        List<ProductEntity> products = productApi.getByIds(foundProductIds);
        
        if (products.size() != foundProductIds.size()) {
            throw new ApiException(ApiStatus.FORBIDDEN, "Client is disabled");
        }
        
        Map<Integer, ProductEntity> productMap = products.stream()
                .collect(Collectors.toMap(ProductEntity::getId, product -> product));
        
        // Extract unique client IDs and check if any are disabled in a single DB call
        List<Integer> clientIds = productMap.values().stream()
                .map(ProductEntity::getClientId)
                .distinct()
                .toList();
        
        List<Integer> disabledClientIds = clientApi.getDisabledClientIds(clientIds);
        
        for (InventoryEntity inventory : inventories) {
            ProductEntity product = productMap.get(inventory.getProductId());
            if (product == null || disabledClientIds.contains(product.getClientId())) {
                throw new ApiException(ApiStatus.FORBIDDEN, "Client is disabled");
            }
        }
        
        return inventories;
    }

    public Page<InventoryEntity> listForEnabledClients(Pageable pageable) {
        return inventoryApi.listForEnabledClients(pageable);
    }

    public com.increff.pos.model.data.PagedResponse<InventoryData> listForEnabledClientsWithData(org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<InventoryEntity> page = listForEnabledClients(pageable);

        if (page.isEmpty()) {
            return new com.increff.pos.model.data.PagedResponse<>(List.of(), 0L);
        }

        List<Integer> productIds = page.getContent().stream()
                .map(InventoryEntity::getProductId)
                .distinct()
                .toList();

        Map<Integer, ProductEntity> productMap = productApi.getByIds(productIds).stream()
                                                .collect(Collectors.toMap(ProductEntity::getId, p -> p));

        List<InventoryData> data = page.getContent().stream()
                .map(inv -> ConversionUtil.inventoryEntityToData(
                        inv,
                        productMap.get(inv.getProductId())
                ))
                .toList();

        return new com.increff.pos.model.data.PagedResponse<>(data, page.getTotalElements());
    }

    public List<InventoryEntity> listAllForEnabledClients() {
        return inventoryApi.listAllForEnabledClients();
    }

    public List<InventoryData> listAllForEnabledClientsWithData() {
        List<InventoryEntity> inventories = listAllForEnabledClients();
        
        if (inventories.isEmpty()) {
            return List.of();
        }

        List<Integer> productIds = inventories.stream()
                .map(InventoryEntity::getProductId)
                .distinct()
                .toList();

        Map<Integer, ProductEntity> productMap =
                productApi.getByIds(productIds).stream()
                        .collect(Collectors.toMap(ProductEntity::getId, p -> p));

        return inventories.stream()
                .map(inv -> ConversionUtil.inventoryEntityToData(
                        inv,
                        productMap.get(inv.getProductId())
                ))
                .toList();
    }

    public List<InventoryData> bulkUpsertAndGetData(List<InventoryEntity> inventories) {
        List<InventoryEntity> saved = bulkUpsert(inventories);
        
        List<Integer> productIds = saved.stream()
                .map(InventoryEntity::getProductId)
                .distinct()
                .toList();

        Map<Integer, ProductEntity> productMap =
                productApi.getByIds(productIds).stream()
                        .collect(Collectors.toMap(ProductEntity::getId, p -> p));

        return saved.stream()
                .map(inv -> ConversionUtil.inventoryEntityToData(
                        inv,
                        productMap.get(inv.getProductId())
                ))
                .toList();
    }

    public List<InventoryEntity> bulkUpsert(List<InventoryEntity> inventories) {
        Map<Integer, ProductEntity> productMap = getProductIds(inventories);

        // Extract unique client IDs and check if any are disabled in a single DB call
        List<Integer> clientIds = productMap.values().stream()
                .map(ProductEntity::getClientId)
                .distinct()
                .toList();
        
        List<Integer> disabledClientIds = clientApi.getDisabledClientIds(clientIds);

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

            if (disabledClientIds.contains(product.getClientId())) {
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
