package com.increff.pos.dto;

import com.increff.pos.entity.InventoryEntity;
import com.increff.pos.entity.ProductEntity;
import com.increff.pos.flow.InventoryFlow;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.data.PagedResponse;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.api.ProductApi;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import com.increff.pos.model.form.InventorySearchForm;
import com.increff.pos.util.ConversionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.increff.pos.util.Utils.parse;

@Service
public class InventoryDto extends AbstractDto {

    @Autowired
    private InventoryFlow inventoryFlow;

    @Autowired
    private ProductApi productApi;

    public InventoryData upsert(InventoryForm form) {
        checkValid(form);

        InventoryEntity saved = inventoryFlow.upsert(ConversionUtil.inventoryFormToEntity(form));

        ProductEntity product = productApi.getProductById(form.getProductId());
        return ConversionUtil.inventoryEntityToData(saved, product);
    }

    public PagedResponse<InventoryData> list(InventorySearchForm form) {

        Pageable pageable = PageRequest.of(
                form.getPage(),
                form.getPageSize(),
                Sort.by("productId").ascending()
        );

        Page<InventoryEntity> page =
                inventoryFlow.listForEnabledClients(pageable);

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

        List<InventoryEntity> saved =
                inventoryFlow.bulkUpsert(
                        forms.stream()
                                .map(ConversionUtil::inventoryFormToEntity)
                                .toList()
                );

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

    public List<InventoryData> uploadTsv(MultipartFile file) {

        List<InventoryForm> forms = parseInventoryTsv(file);
        return bulkUpsert(forms);
    }

    public List<InventoryForm> parseInventoryTsv(MultipartFile file) {

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

        return forms;
    }

    private void validateProductId(Integer productId) {
        if (Objects.isNull(productId)) {
            throw new ApiException(ApiStatus.BAD_DATA, "Product ID is required", "productId", "Product ID is required");
        }
    }

}
