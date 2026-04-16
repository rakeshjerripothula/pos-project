package com.increff.pos.flow;

import com.increff.pos.api.ClientApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.entity.ProductEntity;
import com.increff.pos.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductFlow {

    @Autowired
    private ProductApi productApi;

    @Autowired
    private ClientApi clientApi;

    @Transactional(readOnly = true)
    public List<ProductEntity> getAll(){
        return productApi.getAll();
    }

    public ProductEntity createProduct(ProductEntity product) {
        clientApi.checkClientEnabled(product.getClientId());
        return productApi.createProduct(product);
    }

    @Transactional(rollbackFor = ApiException.class)
    public ProductEntity createFromUpload(ProductEntity uploadEntity) {
        clientApi.checkClientEnabled(uploadEntity.getClientId());
        productApi.createProduct(uploadEntity);
        return uploadEntity;
    }

    public ProductEntity updateProduct(Integer productId, ProductEntity product) {
        ProductEntity existing = productApi.getCheckProductById(productId);
        clientApi.checkClientEnabled(existing.getClientId());
        return productApi.updateProduct(productId, product);
    }

    @Transactional(readOnly = true)
    public Page<ProductEntity> searchProducts(Integer clientId, String barcode, String productName, Pageable pageable) {
        if (clientId != null) clientApi.checkClientEnabled(clientId);
        Page<ProductEntity> page = productApi.searchProducts(clientId,barcode,productName,pageable);
        List<Integer> clientIds = page.getContent().stream().map(ProductEntity::getClientId).distinct().toList();
        List<Integer> enabledClientIds = clientApi.getEnabledClientIds(clientIds,true);
        List<ProductEntity> filtered = page.getContent().stream().filter(p -> enabledClientIds.contains(p.getClientId())).toList();
        return new PageImpl<>(filtered,pageable,page.getTotalElements());
    }

}
