package com.increff.pos.dto;

import com.increff.pos.entity.ProductEntity;
import com.increff.pos.model.data.PagedResponse;
import com.increff.pos.model.data.ProductData;
import com.increff.pos.model.data.TsvUploadError;
import com.increff.pos.model.data.TsvUploadResult;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.api.ProductApi;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import com.increff.pos.model.form.ProductSearchForm;
import com.increff.pos.util.ConversionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.increff.pos.util.Utils.parse;

@Service
public class ProductDto extends AbstractDto {

    @Autowired
    private ProductApi productApi;

    public ProductData createProduct(ProductForm form) {
        checkValid(form);
        ProductEntity product = ConversionUtil.productFormToEntity(form);
        return ConversionUtil.productEntityToData(productApi.createProduct(product));
    }

    public ProductData updateProduct(Integer productId, ProductForm form) {
        if (Objects.isNull(productId)) {
            throw new ApiException(ApiStatus.BAD_DATA, "Product ID is required", "productId", "Product ID is required");
        }
        
        checkValid(form);
        ProductEntity product = ConversionUtil.productFormToEntity(form);
        return ConversionUtil.productEntityToData(productApi.updateProduct(productId, product));
    }

    public List<ProductData> getAll() {
        List<ProductEntity> entities = productApi.getAll();
        return entities.stream()
                .map(ConversionUtil::productEntityToData)
                .toList();
    }

    public PagedResponse<ProductData> listProducts(ProductSearchForm form) {

        Pageable pageable = PageRequest.of(form.getPage(),
                form.getPageSize(),
                Sort.by("productName").ascending()
        );

        Page<ProductEntity> page = productApi.listProductsForEnabledClients(pageable);

        List<ProductData> data = page.getContent()
                .stream()
                .map(ConversionUtil::productEntityToData)
                .toList();

        return new PagedResponse<>(data, page.getTotalElements());
    }

    public ProductData getById(Integer id) {
        if (Objects.isNull(id)) {
            throw new ApiException(ApiStatus.BAD_DATA, "Product ID is required", "id", "Product ID is required");
        }
        return ConversionUtil.productEntityToData(productApi.getProductById(id));
    }

    public List<ProductData> bulkCreateProducts(List<ProductForm> forms) {
        checkValidList(forms);

        List<ProductEntity> entities = forms.stream()
                .map(ConversionUtil::productFormToEntity)
                .toList();

        List<ProductEntity> saved = productApi.bulkCreateProducts(entities);

        return saved.stream()
                .map(ConversionUtil::productEntityToData)
                .toList();
    }

    public TsvUploadResult<ProductData> uploadProductsTsv(MultipartFile file) {
        TsvUploadResult<ProductForm> parseResult = parseProductTsv(file);
        
        if (!parseResult.isSuccess()) {
            return TsvUploadResult.failure(parseResult.getErrors());
        }
        
        List<ProductData> savedData = bulkCreateProducts(parseResult.getData());
        return TsvUploadResult.success(savedData);
    }

    public TsvUploadResult<ProductForm> parseProductTsv(MultipartFile file) {

        List<String[]> rows = parse(file);
        List<ProductForm> forms = new ArrayList<>();
        List<TsvUploadError> errors = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            String[] r = rows.get(i);
            int rowNumber = i + 2; // +2 because: 0-based index + header row

            if (r.length < 4) {
                errors.add(new TsvUploadError(
                    rowNumber,
                    r,
                    "Expected at least 4 columns, found " + r.length
                ));
                continue;
            }

            try {
                ProductForm form = new ProductForm();
                
                // Validate product name
                if (r[0] == null || r[0].trim().isEmpty()) {
                    errors.add(new TsvUploadError(
                        rowNumber,
                        r,
                        "Product name is required"
                    ));
                    continue;
                }
                form.setProductName(r[0].trim());

                // Validate MRP
                try {
                    BigDecimal mrp = new BigDecimal(r[1].trim()).setScale(2, RoundingMode.HALF_UP);
                    if (mrp.compareTo(BigDecimal.ZERO) <= 0) {
                        errors.add(new TsvUploadError(
                            rowNumber,
                            r,
                            "MRP must be greater than 0"
                        ));
                        continue;
                    }
                    form.setMrp(mrp);
                } catch (NumberFormatException e) {
                    errors.add(new TsvUploadError(
                        rowNumber,
                        r,
                        "Invalid MRP format: " + r[1]
                    ));
                    continue;
                }

                // Validate client ID
                try {
                    Integer clientId = Integer.parseInt(r[2].trim());
                    if (clientId <= 0) {
                        errors.add(new TsvUploadError(
                            rowNumber,
                            r,
                            "Client ID must be greater than 0"
                        ));
                        continue;
                    }
                    form.setClientId(clientId);
                } catch (NumberFormatException e) {
                    errors.add(new TsvUploadError(
                        rowNumber,
                        r,
                        "Invalid client ID format: " + r[2]
                    ));
                    continue;
                }

                // Validate barcode
                if (r[3] == null || r[3].trim().isEmpty()) {
                    errors.add(new TsvUploadError(
                        rowNumber,
                        r,
                        "Barcode is required"
                    ));
                    continue;
                }
                form.setBarcode(r[3].trim());

                // Optional image URL
                if (r.length > 4 && r[4] != null && !r[4].trim().isEmpty()) {
                    form.setImageUrl(r[4].trim());
                }

                forms.add(form);

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


}
