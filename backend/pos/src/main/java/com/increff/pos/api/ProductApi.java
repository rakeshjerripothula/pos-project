package com.increff.pos.api;

import com.increff.pos.dao.ProductDao;
import com.increff.pos.entity.ProductEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@Transactional
public class ProductApi {

    @Autowired
    private ProductDao productDao;

    @Autowired
    private ClientApi clientApi;

    public ProductEntity createProduct(ProductEntity product) {

        if (!clientApi.isClientEnabled(product.getClientId())) {
            throw new ApiException(ApiStatus.FORBIDDEN, "Client is disabled", "clientId", "Client is disabled");
        }

        if (productDao.existsByBarcode(product.getBarcode())) {
            throw new ApiException(ApiStatus.CONFLICT, "Barcode already exists", "barcode", "Barcode already exists");
        }

        return productDao.save(product);
    }

    public ProductEntity updateProduct(Integer productId, ProductEntity product) {

        ProductEntity existing = productDao.findById(productId)
                .orElseThrow(() -> new ApiException(ApiStatus.NOT_FOUND, "Product not found", "productId", "Product not found"));

        if (!clientApi.isClientEnabled(product.getClientId())) {
            throw new ApiException(ApiStatus.FORBIDDEN, "Client is disabled", "clientId", "Client is disabled");
        }

        if (productDao.existsByBarcodeAndIdNot(
                product.getBarcode(), productId)) {
            throw new ApiException(ApiStatus.CONFLICT, "Barcode already exists", "barcode", "Barcode already exists");
        }

        existing.setProductName(product.getProductName());
        existing.setMrp(product.getMrp());
        existing.setClientId(product.getClientId());
        existing.setBarcode(product.getBarcode());
        existing.setImageUrl(product.getImageUrl());

        return productDao.save(existing);
    }

    public List<ProductEntity> listProductsForEnabledClients() {
        return productDao.findProductsForEnabledClients();
    }

    public ProductEntity getProductById(Integer id) {
        return productDao.findById(id)
                .orElseThrow(() -> new ApiException(ApiStatus.NOT_FOUND, "Product not found", "id", "Product not found"));
    }

    public List<ProductEntity> bulkCreateProducts(List<ProductEntity> products) {

        for (ProductEntity product : products) {

            if (!clientApi.isClientEnabled(product.getClientId())) {
                throw new ApiException(
                    ApiStatus.FORBIDDEN,
                    "Client disabled for product: " + product.getProductName(),
                    "clientId",
                    "Client disabled for product: " + product.getProductName()
                );
            }

            if (productDao.existsByBarcode(product.getBarcode())) {
                throw new ApiException(
                    ApiStatus.CONFLICT,
                    "Duplicate barcode: " + product.getBarcode(),
                    "barcode",
                    "Duplicate barcode: " + product.getBarcode()
                );
            }
        }

        return productDao.saveAll(products);
    }

    public List<ProductEntity> getByIds(List<Integer> ids) {
        return productDao.findAllById(ids);
    }
}
