package com.increff.pos.dto;

import com.increff.pos.entity.InventoryEntity;
import com.increff.pos.flow.InventoryFlow;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.data.PagedResponse;
import com.increff.pos.model.data.TsvUploadError;
import com.increff.pos.model.data.TsvUploadResult;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import com.increff.pos.model.form.InventorySearchForm;
import com.increff.pos.util.ConversionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.Objects;

import static com.increff.pos.util.Utils.parse;

@Service
public class InventoryDto extends AbstractDto {

    @Autowired
    private InventoryFlow inventoryFlow;

    public InventoryData upsert(InventoryForm form) {
        checkValid(form);
        return inventoryFlow.upsertAndGetData(ConversionUtil.inventoryFormToEntity(form));
    }

    public PagedResponse<InventoryData> list(InventorySearchForm form) {

        Pageable pageable = PageRequest.of(
                form.getPage(),
                form.getPageSize(),
                Sort.by("productId").ascending()
        );

        return inventoryFlow.searchForEnabledClientsWithData(
                form.getBarcode(),
                form.getProductName(),
                pageable
        );
    }


    public List<InventoryData> getAll() {
        return inventoryFlow.listAllForEnabledClientsWithData();
    }

    public InventoryData getByProductId(Integer productId) {
        validateProductId(productId);
        return inventoryFlow.getByProductIdWithData(productId);
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

        return inventoryFlow.bulkUpsertAndGetData(
                forms.stream()
                        .map(ConversionUtil::inventoryFormToEntity)
                        .toList()
        );
    }

    public TsvUploadResult<InventoryData> uploadTsv(MultipartFile file) {
        TsvUploadResult<InventoryForm> parseResult = parseInventoryTsv(file);
        
        if (!parseResult.isSuccess()) {
            return TsvUploadResult.failure(parseResult.getErrors());
        }
        
        List<InventoryData> savedData = bulkUpsert(parseResult.getData());
        return TsvUploadResult.success(savedData);
    }

    public TsvUploadResult<InventoryForm> parseInventoryTsv(MultipartFile file) {

        List<String[]> rows = parse(file);
        List<InventoryForm> forms = new ArrayList<>();
        List<TsvUploadError> errors = new ArrayList<>();
        Set<Integer> seenProductIds = new HashSet<>();

        for (int i = 0; i < rows.size(); i++) {
            String[] r = rows.get(i);
            int rowNumber = i + 2; // +2 because: 0-based index + header row

            if (r.length < 2) {
                errors.add(new TsvUploadError(
                    rowNumber,
                    r,
                    "Expected at least 2 columns, found " + r.length
                ));
                continue;
            }

            try {
                Integer productId;
                try {
                    productId = Integer.parseInt(r[0].trim());
                    if (productId <= 0) {
                        errors.add(new TsvUploadError(
                            rowNumber,
                            r,
                            "Product ID must be greater than 0"
                        ));
                        continue;
                    }
                } catch (NumberFormatException e) {
                    errors.add(new TsvUploadError(
                        rowNumber,
                        r,
                        "Invalid product ID format: " + r[0]
                    ));
                    continue;
                }

                // Check for duplicate product IDs
                if (!seenProductIds.add(productId)) {
                    errors.add(new TsvUploadError(
                        rowNumber,
                        r,
                        "Duplicate product ID: " + productId
                    ));
                    continue;
                }

                Integer quantity;
                try {
                    quantity = Integer.parseInt(r[1].trim());
                    if (quantity < 0) {
                        errors.add(new TsvUploadError(
                            rowNumber,
                            r,
                            "Quantity cannot be negative"
                        ));
                        continue;
                    }
                } catch (NumberFormatException e) {
                    errors.add(new TsvUploadError(
                        rowNumber,
                        r,
                        "Invalid quantity format: " + r[1]
                    ));
                    continue;
                }

                InventoryForm f = new InventoryForm();
                f.setProductId(productId);
                f.setQuantity(quantity);
                forms.add(f);

            } catch (Exception e) {
                errors.add(new TsvUploadError(
                    rowNumber,
                    r,
                    "Unexpected error: " + e.getMessage()
                ));
            }
        }

        if (!errors.isEmpty()) {
            return TsvUploadResult.failure(errors);
        }

        return TsvUploadResult.success(forms);
    }

    private void validateProductId(Integer productId) {
        if (Objects.isNull(productId)) {
            throw new ApiException(ApiStatus.BAD_DATA, "Product ID is required", "productId", "Product ID is required");
        }
    }

}
