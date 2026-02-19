package com.increff.pos.flow;

import com.increff.pos.api.ClientApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.entity.ProductEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import com.increff.pos.model.internal.ProductUploadModel;
import com.increff.pos.util.ConversionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

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
    public ProductEntity createFromUpload(ProductUploadModel upload, Set<String> compositeSet) {

        Integer clientId = clientApi.getClientIdByName(upload.getClientName());

        clientApi.checkClientEnabled(clientId);

        String compositeKey = clientId + "|" + upload.getProductName() + "|" + upload.getMrp();

        if (!compositeSet.add(compositeKey)) {
            throw new ApiException(ApiStatus.CONFLICT, "Duplicate product combination in file");
        }

        ProductEntity entity = ConversionUtil.convertProductUploadToEntity(upload, clientId);

        productApi.createProduct(entity);

        return entity;
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
