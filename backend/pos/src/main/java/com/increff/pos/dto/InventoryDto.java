package com.increff.pos.dto;

import com.increff.pos.entity.InventoryEntity;
import com.increff.pos.entity.ProductEntity;
import com.increff.pos.flow.InventoryFlow;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.api.ProductApi;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import com.increff.pos.util.ConversionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.Objects;

import static com.increff.pos.util.Utils.parse;

@Service
public class InventoryDto extends AbstractDto {

    @Autowired
    private InventoryFlow inventoryFlow;

    @Autowired
    private ProductApi productApi;

    public InventoryData upsert(InventoryForm form) {
        checkValid(form);

        InventoryEntity saved =
                inventoryFlow.upsert(ConversionUtil.inventoryFormToEntity(form));

        ProductEntity product = productApi.getProductById(form.getProductId());
        return ConversionUtil.inventoryEntityToData(saved, product);
    }

    public List<InventoryData> list() {
        return inventoryFlow.listForEnabledClients().stream()
                .map(inventory -> {
                    ProductEntity product = productApi.getProductById(inventory.getProductId());
                    return ConversionUtil.inventoryEntityToData(inventory, product);
                })
                .toList();
    }

    public InventoryData getByProductId(Integer productId) {
        validateProductId(productId);
        InventoryEntity inventory = inventoryFlow.getByProductId(productId);
        ProductEntity product = productApi.getProductById(productId);
        return ConversionUtil.inventoryEntityToData(inventory, product);
    }

    public List<InventoryData> bulkUpsert(List<InventoryForm> forms) {
        checkValidList(forms);

        Set<Integer> seen = new HashSet<>();
        for (InventoryForm f : forms) {
            if (!seen.add(f.getProductId())) {
                throw new ApiException(
                    ApiStatus.BAD_DATA,
                    "Duplicate productId in bulk upload: " + f.getProductId(),
                    "productId",
                    "Duplicate productId: " + f.getProductId()
                );
            }
        }

        return inventoryFlow.bulkUpsert(
                        forms.stream()
                                .map(ConversionUtil::inventoryFormToEntity)
                                .toList()
                ).stream()
                .map(inventory -> {
                    ProductEntity product = productApi.getProductById(inventory.getProductId());
                    return ConversionUtil.inventoryEntityToData(inventory, product);
                })
                .toList();
    }

    public List<InventoryData> uploadTsv(MultipartFile file) {

        List<String[]> rows = parse(file);
        List<InventoryForm> forms = new ArrayList<>();
        Set<Integer> seenProductIds = new HashSet<>();

        for (int i = 0; i < rows.size(); i++) {
            String[] r = rows.get(i);

            if (r.length < 2) {
                throw new ApiException(
                    ApiStatus.BAD_DATA,
                    "Invalid inventory TSV at line " + (i + 2),
                    "file",
                    "Invalid format at line " + (i + 2)
                );
            }

            Integer productId = Integer.parseInt(r[0].trim());
            Integer quantity = Integer.parseInt(r[1].trim());

            if (!seenProductIds.add(productId)) {
                throw new ApiException(
                    ApiStatus.BAD_DATA,
                    "Duplicate productId in TSV: " + productId,
                    "productId",
                    "Duplicate productId: " + productId
                );
            }

            InventoryForm f = new InventoryForm();
            f.setProductId(productId);
            f.setQuantity(quantity);
            forms.add(f);
        }

        return bulkUpsert(forms);
    }

    private void validateProductId(Integer productId) {
        if (Objects.isNull(productId)) {
            throw new ApiException(ApiStatus.BAD_DATA, "Product ID is required", "productId", "Product ID is required");
        }
    }

}
