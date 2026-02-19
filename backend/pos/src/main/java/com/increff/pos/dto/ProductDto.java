package com.increff.pos.dto;

import com.increff.pos.entity.ProductEntity;
import com.increff.pos.model.data.PagedResponse;
import com.increff.pos.model.data.ProductData;
import com.increff.pos.model.data.TsvUploadError;
import com.increff.pos.model.data.TsvUploadResult;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.flow.ProductFlow;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import com.increff.pos.exception.TsvUploadException;
import com.increff.pos.model.form.ProductSearchForm;
import com.increff.pos.model.form.ProductUploadForm;
import com.increff.pos.util.ConversionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static com.increff.pos.util.TsvParseUtils.parseProductTsv;

@Service
public class ProductDto extends AbstractDto {

    @Autowired
    private ProductFlow productFlow;

    @PreAuthorize("hasAnyRole('OPERATOR','SUPERVISOR')")
    public List<ProductData> getAll() {
        return productFlow.getAll().stream().map(ConversionUtil::productEntityToData).toList();
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    public ProductData createProduct(ProductForm form) {
        checkValid(form);
        ProductEntity entity = ConversionUtil.productFormToEntity(form);
        return ConversionUtil.productEntityToData(productFlow.createProduct(entity));
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    public ProductData updateProduct(Integer productId, ProductForm form) {
        if (Objects.isNull(productId)) throw new ApiException(ApiStatus.BAD_REQUEST,"Product ID is required",
                "productId","Product ID is required");
        checkValid(form);
        ProductEntity entity = ConversionUtil.productFormToEntity(form);
        return ConversionUtil.productEntityToData(productFlow.updateProduct(productId,entity));
    }

    @PreAuthorize("hasAnyRole('OPERATOR','SUPERVISOR')")
    public PagedResponse<ProductData> listProducts(ProductSearchForm form) {
        checkValid(form);
        Pageable pageable = PageRequest.of(form.getPage(),form.getPageSize(),
                                                        Sort.by("createdAt").descending());
        Page<ProductEntity> page = productFlow.searchProducts(form.getClientId(),form.getBarcode(),
                                                        form.getProductName(),pageable);
        List<ProductData> data = page.getContent().stream().map(ConversionUtil::productEntityToData).toList();
        return new PagedResponse<>(data,page.getTotalElements());
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    public TsvUploadResult<ProductData> uploadProductsTsv(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new ApiException(ApiStatus.BAD_REQUEST, "Empty file");
        }

        List<ProductUploadForm> uploadForms = parseProductTsv(file);

        TsvUploadResult<ProductUploadForm> structuralResult = validateUploadForms(uploadForms);

        if (!structuralResult.isSuccess()) {
            throw new TsvUploadException(structuralResult.getErrors(), ApiStatus.BAD_REQUEST);
        }

        TsvUploadResult<ProductEntity> businessResult = validateBusiness(structuralResult.getData());

        if (!businessResult.isSuccess()) {
            throw new TsvUploadException(businessResult.getErrors(), ApiStatus.BAD_REQUEST);
        }

        List<ProductData> saved = productFlow.createProducts(businessResult.getData()).stream()
                                        .map(ConversionUtil::productEntityToData).toList();

        return TsvUploadResult.success(saved);
    }

    private TsvUploadResult<ProductUploadForm> validateUploadForms( List<ProductUploadForm> forms) {
        List<TsvUploadError> errors = new ArrayList<>();
        Set<String> barcodeSet = new HashSet<>();
        for (int i = 0; i < forms.size(); i++) {
            ProductUploadForm form = forms.get(i);
            int rowNumber = i + 2;
            try {
                checkValid(form);
                if (!barcodeSet.add(form.getBarcode())) {
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

    private TsvUploadResult<ProductEntity> validateBusiness(List<ProductUploadForm> forms) {
        List<TsvUploadError> errors = new ArrayList<>();
        List<ProductEntity> validEntities = new ArrayList<>();
        Set<String> compositeSet = new HashSet<>();
        for (int i = 0; i < forms.size(); i++) {
            ProductUploadForm uploadForm = forms.get(i);
            int rowNumber = i + 2;
            try {
                ProductEntity entity = productFlow.validateForUpload(uploadForm, compositeSet);
                validEntities.add(entity);
            } catch (ApiException e) {
                errors.add(new TsvUploadError(rowNumber, null, e.getMessage()));
            }
        }
        if (!errors.isEmpty()) {
            return TsvUploadResult.failure(errors);
        }
        return TsvUploadResult.success(validEntities);
    }

}