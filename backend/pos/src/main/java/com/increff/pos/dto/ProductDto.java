package com.increff.pos.dto;

import com.increff.pos.entity.ProductEntity;
import com.increff.pos.model.data.ProductData;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.api.ProductApi;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import com.increff.pos.util.ConversionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    public List<ProductData> getAllProducts() {
        return productApi.listProductsForEnabledClients()
                .stream()
                .map(ConversionUtil::productEntityToData)
                .collect(Collectors.toList());
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

        List<ProductEntity> saved =
                productApi.bulkCreateProducts(entities);

        return saved.stream()
                .map(ConversionUtil::productEntityToData)
                .toList();
    }

    public List<ProductData> uploadProductsTsv(MultipartFile file) {

        List<String[]> rows = parse(file);
        List<ProductForm> forms = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            String[] r = rows.get(i);

            if (r.length < 4) {
                throw new ApiException(
                    ApiStatus.BAD_DATA,
                    "Invalid product TSV at line " + (i + 2),
                    "file",
                    "Invalid format at line " + (i + 2)
                );
            }

            ProductForm f = new ProductForm();
            f.setProductName(r[0].trim());
            f.setMrp(new BigDecimal(r[1].trim()).setScale(2, RoundingMode.HALF_UP));
            f.setClientId(Integer.parseInt(r[2].trim()));
            f.setBarcode(r[3].trim());
            f.setImageUrl(r.length > 4 ? r[4].trim() : null);

            forms.add(f);
        }

        return bulkCreateProducts(forms);
    }

}
