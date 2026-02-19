package com.increff.pos.flow;

import com.increff.pos.api.ClientApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.entity.ProductEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import com.increff.pos.model.data.ProductData;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.model.form.ProductUploadForm;
import com.increff.pos.util.ConversionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.increff.pos.util.ConversionUtil.convertUploadToProductForm;
import static com.increff.pos.util.ConversionUtil.normalize;

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

    public ProductEntity validateForUpload(ProductUploadForm uploadForm, Set<String> compositeSet) {

        Integer clientId = clientApi.getClientIdByName(uploadForm.getClientName());

        clientApi.checkClientEnabled(clientId);

        String compositeKey = clientId + "|" + uploadForm.getProductName() + "|" + uploadForm.getMrp();

        if (!compositeSet.add(compositeKey)) {
            throw new ApiException(ApiStatus.CONFLICT, "Duplicate product combination in file");
        }

        ProductForm form = convertUploadToProductForm(uploadForm, clientId);

        return ConversionUtil.productFormToEntity(form);
    }

    @Transactional(rollbackFor = ApiException.class)
    public List<ProductEntity> createProducts(List<ProductEntity> products) {

        for (ProductEntity product : products) {
            productApi.createProduct(product);
        }

        return products;
    }

    public ProductEntity createProduct(ProductEntity product) {
        clientApi.checkClientEnabled(product.getClientId());
        return productApi.createProduct(product);
    }

    public ProductEntity updateProduct(Integer productId, ProductEntity product) {
        ProductEntity existing = productApi.getProductById(productId);
        clientApi.checkClientEnabled(existing.getClientId());
        return productApi.updateProduct(productId,product);
    }

    @Transactional(readOnly = true)
    public Page<ProductEntity> searchProducts(Integer clientId, String barcode, String productName, Pageable pageable) {
        if (clientId != null) clientApi.checkClientEnabled(clientId);
        Page<ProductEntity> page = productApi.searchProducts(clientId,barcode,productName,pageable);
        List<Integer> clientIds = page.getContent().stream().map(ProductEntity::getClientId).distinct().toList();
        List<Integer> enabledClientIds = clientApi.getEnabledClientIds(clientIds,true);
        List<ProductEntity> filtered = page.getContent().stream().filter(p -> enabledClientIds.contains(p.getClientId())).toList();
        return new PageImpl<>(filtered,pageable,filtered.size());
    }

    public ProductEntity getByName(String name) {
        return productApi.getByName(name);
    }

    public List<ProductEntity> getByNames(List<String> names) {
        return productApi.getByNames(names);
    }

    public ProductEntity getProductById(Integer id) {
        return productApi.getProductById(id);
    }

    public List<ProductEntity> getByIds(List<Integer> ids) {
        return productApi.getByIds(ids);
    }
}
