package com.increff.pos.api;

import com.increff.pos.dao.ProductDao;
import com.increff.pos.entity.ProductEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class ProductApi {

    @Autowired
    private ProductDao productDao;

    @Transactional(readOnly = true)
    public List<ProductEntity> getAll() {
        return productDao.selectAll();
    }

    @Transactional(readOnly = true)
    public ProductEntity getCheckProductById(Integer id) {
        return productDao.selectById(id).orElseThrow(() ->
                new ApiException(ApiStatus.NOT_FOUND, "Product not found", "productId", "Product not found"));
    }

    public List<ProductEntity> getByIds(List<Integer> ids) {
        return productDao.selectByIds(ids);
    }

    public ProductEntity createProduct(ProductEntity product) {

        validateProduct(product);

        return productDao.save(product);
    }

    public ProductEntity updateProduct(Integer productId, ProductEntity product) {

        ProductEntity existing = productDao.selectById(productId).orElseThrow(() ->
                new ApiException(ApiStatus.NOT_FOUND, "Product not found", "productId", "Product not found"));

        if (!existing.getClientId().equals(product.getClientId())) {
            throw new ApiException(ApiStatus.BAD_REQUEST, "Client cannot be changed for product", "clientId",
                    "Client change not allowed");
        }

        if (productDao.selectByBarcodeExcludingId(product.getBarcode(), productId).isPresent()) {
            throw new ApiException(ApiStatus.CONFLICT, "Barcode already exists", "barcode " + product.getBarcode(), "Barcode already exists");
        }

        if (productDao.selectByClientIdAndProductNameAndMrpExcludingId(product.getClientId(), product.getProductName(),
                product.getMrp(), productId).isPresent()) {
            throw new ApiException(ApiStatus.CONFLICT, "Product already exists for this client with same name and MRP",
                    "product", "Duplicate product for client");
        }
        existing.setProductName(product.getProductName());
        existing.setMrp(product.getMrp());
        existing.setBarcode(product.getBarcode());
        existing.setImageUrl(product.getImageUrl());

        return productDao.save(existing);
    }

    @Transactional(readOnly = true)
    public Page<ProductEntity> searchProducts(Integer clientId, String barcode, String productName, Pageable pageable) {
        return productDao.selectByFilters(clientId, barcode, productName, pageable);
    }

    public ProductEntity getCheckByName(String name) {
        ProductEntity product = productDao.selectByName(name);

        if(Objects.isNull(product)) {
                throw new ApiException(ApiStatus.NOT_FOUND, "Product not found: " + name, "productName",
                    "Product not found: " + name);
        }
        return product;
    }

    public ProductEntity getCheckByBarcode(String barcode) {
        ProductEntity product = productDao.selectByBarcode(barcode).orElse(null);

        if(Objects.isNull(product)) {
                throw new ApiException(ApiStatus.NOT_FOUND, "Product not found: " + barcode, "barcode",
                    "Product not found: " + barcode);
        }
        return product;
    }

    public void validateProduct(ProductEntity product) {
        if (productDao.selectByBarcode(product.getBarcode()).isPresent()) {
            throw new ApiException(ApiStatus.CONFLICT, "Barcode already exists", "barcode " + product.getBarcode(), "Barcode already exists");
        }

        if (productDao.selectByClientIdAndProductNameAndMrp(product.getClientId(), product.getProductName(),
                product.getMrp()).isPresent()) {
            throw new ApiException(ApiStatus.CONFLICT, "Product already exists for this client with same name and MRP",
                    "product", "Duplicate product for client");
        }
    }
}
