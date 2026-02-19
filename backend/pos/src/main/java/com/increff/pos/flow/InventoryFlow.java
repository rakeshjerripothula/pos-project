package com.increff.pos.flow;

import com.increff.pos.api.ClientApi;
import com.increff.pos.api.InventoryApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.entity.InventoryEntity;
import com.increff.pos.entity.ProductEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.data.PagedResponse;
import com.increff.pos.model.internal.InventoryUploadModel;
import com.increff.pos.util.ConversionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class InventoryFlow {

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private ClientApi clientApi;

    @Autowired
    private ProductApi productApi;

    public List<InventoryData> getAllForEnabledClients() {
        List<InventoryEntity> inventories = inventoryApi.getAllForEnabledClients();
        if (inventories.isEmpty()) return List.of();
        return convertToData(inventories);
    }

    public PagedResponse<InventoryData> getPagedForEnabledClients(String barcode, String productName,
                                                                     Pageable pageable) {
        Page<InventoryEntity> page = inventoryApi.getPagedForEnabledClients(barcode, productName, pageable);
        if (page.isEmpty()) return new PagedResponse<>(List.of(), 0L);
        List<InventoryData> data = convertToData(page.getContent());
        return new PagedResponse<>(data, page.getTotalElements());
    }

    public InventoryData upsert(InventoryEntity inventory) {
        ProductEntity product = validateProductAndClient(inventory.getProductId());
        InventoryEntity saved = inventoryApi.upsert(inventory);
        return ConversionUtil.inventoryEntityToData(saved, product);
    }

    public List<InventoryData> bulkUpsert(List<InventoryEntity> inventories) {
        validateBulkProductsAndClients(inventories);
        List<InventoryEntity> saved = inventories.stream().map(inventoryApi::upsert).toList();
        return convertToData(saved);
    }

    @Transactional(rollbackFor = ApiException.class)
    public InventoryData upsertFromUpload(InventoryUploadModel upload) {

        ProductEntity product = productApi.getCheckByBarcode(upload.getBarcode());

        clientApi.checkClientEnabled(product.getClientId());

        InventoryEntity entity = new InventoryEntity();
        entity.setProductId(product.getId());
        entity.setQuantity(upload.getQuantity());

        InventoryEntity saved = inventoryApi.upsert(entity);

        return ConversionUtil.inventoryEntityToData(saved, product);
    }

    private ProductEntity validateProductAndClient(Integer productId) {
        ProductEntity product = productApi.getCheckProductById(productId);
        clientApi.checkClientEnabled(product.getClientId());
        return product;
    }

    private void validateBulkProductsAndClients(List<InventoryEntity> inventories) {
        Map<Integer, ProductEntity> productMap = buildProductMap(inventories);
        List<Integer> clientIds = productMap.values().stream().map(ProductEntity::getClientId).distinct().toList();
        List<Integer> disabledClientIds = clientApi.getEnabledClientIds(clientIds, false);

        for (InventoryEntity inventory : inventories) {
            ProductEntity product = productMap.get(inventory.getProductId());
            if (product == null)
                throw new ApiException(ApiStatus.NOT_FOUND, "Product not found: " + inventory.getProductId(),
                        "productId", "Product not found: " + inventory.getProductId());
            if (disabledClientIds.contains(product.getClientId()))
                throw new ApiException(ApiStatus.FORBIDDEN, "Client is disabled", "clientId", "Client is disabled");
        }
    }

    private List<InventoryData> convertToData(List<InventoryEntity> inventories) {
        Map<Integer, ProductEntity> productMap = buildProductMap(inventories);
        return inventories.stream()
                .map(inv -> ConversionUtil.inventoryEntityToData(inv, productMap.get(inv.getProductId()))).toList();
    }

    private Map<Integer, ProductEntity> buildProductMap(List<InventoryEntity> inventories) {
        List<Integer> productIds = inventories.stream().map(InventoryEntity::getProductId).distinct().toList();
        return productApi.getByIds(productIds).stream().collect(Collectors.toMap(ProductEntity::getId, p -> p));
    }
}
