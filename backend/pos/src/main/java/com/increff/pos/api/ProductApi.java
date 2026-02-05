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

import java.math.BigDecimal;
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

        if (productDao.existsByClientIdAndProductNameAndMrp(
                product.getClientId(),
                product.getProductName(),
                product.getMrp())) {

            throw new ApiException(
                    ApiStatus.CONFLICT,
                    "Product already exists for this client with same name and MRP",
                    "product",
                    "Duplicate product for client"
            );
        }

        return productDao.save(product);
    }

    public ProductEntity updateProduct(Integer productId, ProductEntity product) {

        ProductEntity existing = productDao.findById(productId)
                .orElseThrow(() -> new ApiException(ApiStatus.NOT_FOUND,
                        "Product not found",
                        "productId",
                        "Product not found"
                ));

        if (!existing.getClientId().equals(product.getClientId())) {
            throw new ApiException(
                    ApiStatus.BAD_REQUEST,
                    "Client cannot be changed for product",
                    "clientId",
                    "Client change not allowed"
            );
        }

        Integer clientId = existing.getClientId();

        if (!clientApi.isClientEnabled(clientId)) {
            throw new ApiException(ApiStatus.FORBIDDEN, "Client is disabled", "clientId", "Client is disabled");
        }

        String newName = product.getProductName();
        BigDecimal newMrp = product.getMrp();
        String newBarcode = product.getBarcode();

        if (productDao.existsByBarcodeAndNotId(newBarcode, productId)) {
            throw new ApiException(
                    ApiStatus.CONFLICT,
                    "Barcode already exists",
                    "barcode",
                    "Barcode already exists"
            );
        }

        if (productDao.existsByClientIdAndProductNameAndMrpAndNotId(
                clientId,
                newName,
                newMrp,
                productId)) {

            throw new ApiException(
                    ApiStatus.CONFLICT,
                    "Product already exists for this client with same name and MRP",
                    "product",
                    "Duplicate product for client"
            );
        }

        existing.setProductName(newName);
        existing.setMrp(newMrp);
        existing.setBarcode(newBarcode);
        existing.setImageUrl(product.getImageUrl());

        return productDao.save(existing);
    }

    public List<ProductEntity> getAll() {
        return productDao.selectAll();
    }

    public Page<ProductEntity> listProductsForEnabledClients(Pageable pageable) {
        return productDao.findProductsForEnabledClients(pageable);
    }

    public ProductEntity getProductById(Integer id) {
        return productDao.findById(id)
                .orElseThrow(() -> new ApiException(ApiStatus.NOT_FOUND, "Product not found", "id", "Product not found"));
    }

    public List<ProductEntity> bulkCreateProducts(List<ProductEntity> products) {

        List<String> barcodes = products.stream()
                .map(ProductEntity::getBarcode)
                .toList();

        List<Integer> clientIds = products.stream()
                .map(ProductEntity::getClientId)
                .distinct()
                .toList();

        List<String> existingBarcodes = productDao.findExistingBarcodes(barcodes);

        if (!existingBarcodes.isEmpty()) {
            throw new ApiException(
                    ApiStatus.CONFLICT,
                    "Duplicate barcodes: " + existingBarcodes,
                    "barcode",
                    "Duplicate barcodes found"
            );
        }

        List<Integer> disabledClients = clientApi.getDisabledClientIds(clientIds);

        if (!disabledClients.isEmpty()) {
            throw new ApiException(
                    ApiStatus.FORBIDDEN,
                    "Disabled clients: " + disabledClients,
                    "clientId",
                    "Some clients are disabled"
            );
        }

        return productDao.saveAll(products);
    }

    public List<ProductEntity> getByIds(List<Integer> ids) {
        return productDao.findAllById(ids);
    }
}
