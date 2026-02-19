package com.increff.pos.dto;

import com.increff.pos.flow.InventoryFlow;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.data.PagedResponse;
import com.increff.pos.model.data.TsvUploadError;
import com.increff.pos.model.data.TsvUploadResult;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import com.increff.pos.exception.TsvUploadException;
import com.increff.pos.model.form.InventorySearchForm;
import com.increff.pos.model.form.InventoryUploadForm;
import com.increff.pos.model.internal.InventoryUploadModel;
import com.increff.pos.util.ConversionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static com.increff.pos.util.TsvParseUtils.parseInventoryTsv;

@Service
public class InventoryDto extends AbstractDto {

    @Autowired
    private InventoryFlow inventoryFlow;

    @PreAuthorize("hasAnyRole('OPERATOR','SUPERVISOR')")
    public List<InventoryData> getAll() {
        return inventoryFlow.getAllForEnabledClients();
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    public InventoryData upsert(InventoryForm form) {
        checkValid(form);
        if (form.getQuantity() < 0) {
            throw new ApiException(
                    ApiStatus.BAD_REQUEST, "Quantity cannot be negative", "quantity", "Quantity cannot be negative"
            );
        }
        return inventoryFlow.upsert(ConversionUtil.inventoryFormToEntity(form));
    }

    @PreAuthorize("hasAnyRole('OPERATOR','SUPERVISOR')")
    public PagedResponse<InventoryData> list(InventorySearchForm form) {

        Pageable pageable = PageRequest.of(
                form.getPage(),
                form.getPageSize(),
                Sort.by("productId").descending()
        );

        return inventoryFlow.getPagedForEnabledClients(form.getBarcode(), form.getProductName(), pageable);
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    public TsvUploadResult<InventoryData> uploadTsv(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new ApiException(ApiStatus.BAD_REQUEST, "Empty file");
        }

        List<InventoryUploadForm> forms = parseInventoryTsv(file);

        TsvUploadResult<InventoryUploadForm> structural = validateStructural(forms);

        if (!structural.isSuccess()) {
            throw new TsvUploadException(structural.getErrors(), ApiStatus.BAD_REQUEST);
        }

        List<InventoryUploadModel> uploads = structural.getData().stream().map(ConversionUtil::inventoryUploadFormToModel).toList();

        return processRowWise(uploads);
    }


    private TsvUploadResult<InventoryUploadForm> validateStructural(List<InventoryUploadForm> forms) {

        List<TsvUploadError> errors = new ArrayList<>();
        Set<String> seenBarcodes = new HashSet<>();
        for (int i = 0; i < forms.size(); i++) {
            InventoryUploadForm form = forms.get(i);
            int rowNumber = i + 2;
            try {
                checkValid(form);
                String normalized = form.getBarcode().trim().toLowerCase();
                if (!seenBarcodes.add(normalized)) {
                    throw new ApiException(ApiStatus.BAD_REQUEST, "Duplicate barcode in file");
                }
            } catch (Exception e) {
                errors.add(new TsvUploadError(rowNumber, null, e.getMessage()));
            }
        }
        if (!errors.isEmpty()) {
            return TsvUploadResult.failure(errors);
        }
        return TsvUploadResult.success(forms);
    }

    private TsvUploadResult<InventoryData> processRowWise(List<InventoryUploadModel> uploads) {

        List<TsvUploadError> errors = new ArrayList<>();
        List<InventoryData> success = new ArrayList<>();
        for (int i = 0; i < uploads.size(); i++) {
            InventoryUploadModel upload = uploads.get(i);
            int rowNumber = i + 2;
            try {
                InventoryData data = inventoryFlow.upsertFromUpload(upload);

                success.add(data);

            } catch (ApiException e) {
                errors.add(new TsvUploadError(rowNumber, null, e.getMessage()));
            }
        }

        if (!errors.isEmpty()) {
            return TsvUploadResult.failure(errors);
        }

        return TsvUploadResult.success(success);
    }

}
