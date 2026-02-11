package com.increff.pos.api;

import com.increff.pos.dao.ProductDao;
import com.increff.pos.entity.ProductEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductApiTest {

    @Mock
    private ProductDao productDao;

    @Mock
    private ClientApi clientApi;

    @InjectMocks
    private ProductApi productApi;

    @Test
    void should_create_product_when_valid_input() {
        // Arrange
        ProductEntity product = new ProductEntity();
        product.setProductName("test product");
        product.setMrp(new BigDecimal("100.00"));
        product.setClientId(1);
        product.setBarcode("12345");
        product.setImageUrl("http://example.com/image.jpg");

        when(clientApi.isClientEnabled(1)).thenReturn(true);
        when(productDao.existsByBarcode("12345")).thenReturn(false);
        when(productDao.save(product)).thenAnswer(invocation -> {
            ProductEntity saved = invocation.getArgument(0);
            saved.setId(1);
            return saved;
        });

        // Act
        ProductEntity result = productApi.createProduct(product);

        // Assert
        assertEquals(1, result.getId());
        assertEquals("test product", result.getProductName());
        verify(clientApi).isClientEnabled(1);
        verify(productDao).existsByBarcode("12345");
        verify(productDao).save(product);
    }

    @Test
    void should_throw_exception_when_creating_product_for_disabled_client() {
        // Arrange
        ProductEntity product = new ProductEntity();
        product.setProductName("test product");
        product.setClientId(1);
        product.setBarcode("12345");

        when(clientApi.isClientEnabled(1)).thenReturn(false);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> productApi.createProduct(product));
        assertEquals(ApiStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Client is disabled", exception.getMessage());
        verify(clientApi).isClientEnabled(1);
        verify(productDao, never()).existsByBarcode(anyString());
        verify(productDao, never()).save(any());
    }

    @Test
    void should_throw_exception_when_creating_product_with_duplicate_barcode() {
        // Arrange
        ProductEntity product = new ProductEntity();
        product.setProductName("test product");
        product.setClientId(1);
        product.setBarcode("12345");

        when(clientApi.isClientEnabled(1)).thenReturn(true);
        when(productDao.existsByBarcode("12345")).thenReturn(true);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> productApi.createProduct(product));
        assertEquals(ApiStatus.CONFLICT, exception.getStatus());
        assertEquals("Barcode already exists", exception.getMessage());
        verify(clientApi).isClientEnabled(1);
        verify(productDao).existsByBarcode("12345");
        verify(productDao, never()).save(any());
    }

    @Test
    void should_update_product_when_valid_input() {
        // Arrange
        ProductEntity existingProduct = new ProductEntity();
        existingProduct.setId(1);
        existingProduct.setProductName("old product");
        existingProduct.setMrp(new BigDecimal("50.00"));
        existingProduct.setClientId(1);
        existingProduct.setBarcode("12345");

        ProductEntity updateData = new ProductEntity();
        updateData.setProductName("new product");
        updateData.setMrp(new BigDecimal("100.00"));
        updateData.setClientId(1);
        updateData.setBarcode("67890");
        updateData.setImageUrl("http://example.com/new-image.jpg");

        when(productDao.findById(1)).thenReturn(Optional.of(existingProduct));
        when(clientApi.isClientEnabled(1)).thenReturn(true);
        when(productDao.existsByBarcodeAndNotId("67890", 1)).thenReturn(false);
        when(productDao.save(any(ProductEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ProductEntity result = productApi.updateProduct(1, updateData);

        // Assert
        assertEquals("new product", result.getProductName());
        assertEquals(new BigDecimal("100.00"), result.getMrp());
        assertEquals("67890", result.getBarcode());
        assertEquals("http://example.com/new-image.jpg", result.getImageUrl());
        verify(productDao).findById(1);
        verify(clientApi).isClientEnabled(1);
        verify(productDao).existsByBarcodeAndNotId("67890", 1);
        verify(productDao).save(existingProduct);
    }

    @Test
    void should_throw_exception_when_updating_product_not_found() {
        // Arrange
        ProductEntity updateData = new ProductEntity();
        updateData.setProductName("new product");

        when(productDao.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> productApi.updateProduct(1, updateData));
        assertEquals(ApiStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Product not found", exception.getMessage());
        verify(productDao).findById(1);
        verify(clientApi, never()).isClientEnabled(anyInt());
        verify(productDao, never()).existsByBarcodeAndNotId(anyString(), anyInt());
        verify(productDao, never()).save(any());
    }

    @Test
    void should_throw_exception_when_updating_product_for_disabled_client() {
        // Arrange
        ProductEntity existingProduct = new ProductEntity();
        existingProduct.setId(1);
        existingProduct.setProductName("old product");
        existingProduct.setClientId(1); // Set clientId to prevent NPE

        ProductEntity updateData = new ProductEntity();
        updateData.setClientId(1);

        when(productDao.findById(1)).thenReturn(Optional.of(existingProduct));
        when(clientApi.isClientEnabled(1)).thenReturn(false);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> productApi.updateProduct(1, updateData));
        assertEquals(ApiStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Client is disabled", exception.getMessage());
        verify(productDao).findById(1);
        verify(clientApi).isClientEnabled(1);
        verify(productDao, never()).existsByBarcodeAndNotId(anyString(), anyInt());
        verify(productDao, never()).save(any());
    }

    @Test
    void should_throw_exception_when_updating_product_with_duplicate_barcode() {
        // Arrange
        ProductEntity existingProduct = new ProductEntity();
        existingProduct.setId(1);
        existingProduct.setProductName("old product");
        existingProduct.setClientId(1); // Set clientId to prevent NPE

        ProductEntity updateData = new ProductEntity();
        updateData.setClientId(1);
        updateData.setBarcode("existing-barcode");

        when(productDao.findById(1)).thenReturn(Optional.of(existingProduct));
        when(clientApi.isClientEnabled(1)).thenReturn(true);
        when(productDao.existsByBarcodeAndNotId("existing-barcode", 1)).thenReturn(true);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> productApi.updateProduct(1, updateData));
        assertEquals(ApiStatus.CONFLICT, exception.getStatus());
        assertEquals("Barcode already exists", exception.getMessage());
        verify(productDao).findById(1);
        verify(clientApi).isClientEnabled(1);
        verify(productDao).existsByBarcodeAndNotId("existing-barcode", 1);
        verify(productDao, never()).save(any());
    }

    @Test
    void should_get_product_by_id_when_exists() {
        // Arrange
        ProductEntity product = new ProductEntity();
        product.setId(1);
        product.setProductName("test product");

        when(productDao.findById(1)).thenReturn(Optional.of(product));

        // Act
        ProductEntity result = productApi.getProductById(1);

        // Assert
        assertEquals(1, result.getId());
        assertEquals("test product", result.getProductName());
        verify(productDao).findById(1);
    }

    @Test
    void should_throw_exception_when_getting_product_by_id_not_found() {
        // Arrange
        when(productDao.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> productApi.getProductById(1));
        assertEquals(ApiStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Product not found", exception.getMessage());
        verify(productDao).findById(1);
    }

    @Test
    void should_bulk_create_products_when_valid_input() {
        // Arrange
        ProductEntity product1 = new ProductEntity();
        product1.setProductName("product 1");
        product1.setClientId(1);
        product1.setBarcode("11111");

        ProductEntity product2 = new ProductEntity();
        product2.setProductName("product 2");
        product2.setClientId(2);
        product2.setBarcode("22222");

        List<ProductEntity> products = List.of(product1, product2);

        when(productDao.findExistingBarcodes(List.of("11111", "22222"))).thenReturn(List.of());
        when(clientApi.getDisabledClientIds(List.of(1, 2))).thenReturn(List.of());
        when(productDao.saveAll(products)).thenAnswer(invocation -> {
            List<ProductEntity> saved = invocation.getArgument(0);
            saved.get(0).setId(1);
            saved.get(1).setId(2);
            return saved;
        });

        // Act
        List<ProductEntity> result = productApi.bulkCreateProducts(products);

        // Assert
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(2, result.get(1).getId());
        verify(productDao).findExistingBarcodes(List.of("11111", "22222"));
        verify(clientApi).getDisabledClientIds(List.of(1, 2));
        verify(productDao).saveAll(products);
    }

    @Test
    void should_throw_exception_when_bulk_creating_products_with_duplicate_barcodes() {
        // Arrange
        ProductEntity product1 = new ProductEntity();
        product1.setBarcode("11111");

        ProductEntity product2 = new ProductEntity();
        product2.setBarcode("22222");

        List<ProductEntity> products = List.of(product1, product2);

        when(productDao.findExistingBarcodes(List.of("11111", "22222"))).thenReturn(List.of("11111"));

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> productApi.bulkCreateProducts(products));
        assertEquals(ApiStatus.CONFLICT, exception.getStatus());
        assertEquals("Duplicate barcodes: [11111]", exception.getMessage());
        verify(productDao).findExistingBarcodes(List.of("11111", "22222"));
        verify(clientApi, never()).getDisabledClientIds(any());
        verify(productDao, never()).saveAll(any());
    }

    @Test
    void should_throw_exception_when_bulk_creating_products_for_disabled_clients() {
        // Arrange
        ProductEntity product1 = new ProductEntity();
        product1.setClientId(1);
        product1.setBarcode("11111");

        ProductEntity product2 = new ProductEntity();
        product2.setClientId(2);
        product2.setBarcode("22222");

        List<ProductEntity> products = List.of(product1, product2);

        when(productDao.findExistingBarcodes(List.of("11111", "22222"))).thenReturn(List.of());
        when(clientApi.getDisabledClientIds(List.of(1, 2))).thenReturn(List.of(1));

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> productApi.bulkCreateProducts(products));
        assertEquals(ApiStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Disabled clients: [1]", exception.getMessage());
        verify(productDao).findExistingBarcodes(List.of("11111", "22222"));
        verify(clientApi).getDisabledClientIds(List.of(1, 2));
        verify(productDao, never()).saveAll(any());
    }

    @Test
    void should_get_products_by_ids_when_multiple_exist() {
        // Arrange
        ProductEntity product1 = new ProductEntity();
        product1.setId(1);

        ProductEntity product2 = new ProductEntity();
        product2.setId(2);

        List<ProductEntity> products = List.of(product1, product2);

        when(productDao.findAllById(List.of(1, 2))).thenReturn(products);

        // Act
        List<ProductEntity> result = productApi.getByIds(List.of(1, 2));

        // Assert
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(2, result.get(1).getId());
        verify(productDao).findAllById(List.of(1, 2));
    }

    @Test
    void should_return_empty_list_when_no_products_found_by_ids() {
        // Arrange
        when(productDao.findAllById(List.of(999, 1000))).thenReturn(List.of());

        // Act
        List<ProductEntity> result = productApi.getByIds(List.of(999, 1000));

        // Assert
        assertEquals(0, result.size());
        verify(productDao).findAllById(List.of(999, 1000));
    }

    @Test
    void should_handle_duplicate_client_ids_in_bulk_create() {
        // Arrange
        ProductEntity product1 = new ProductEntity();
        product1.setClientId(1);
        product1.setBarcode("11111");

        ProductEntity product2 = new ProductEntity();
        product2.setClientId(1); // Same client
        product2.setBarcode("22222");

        List<ProductEntity> products = List.of(product1, product2);

        when(productDao.findExistingBarcodes(List.of("11111", "22222"))).thenReturn(List.of());
        when(clientApi.getDisabledClientIds(List.of(1))).thenReturn(List.of());
        when(productDao.saveAll(products)).thenReturn(products);

        // Act
        List<ProductEntity> result = productApi.bulkCreateProducts(products);

        // Assert
        assertEquals(2, result.size());
        verify(productDao).findExistingBarcodes(List.of("11111", "22222"));
        verify(clientApi).getDisabledClientIds(List.of(1)); // Should be called with distinct client IDs
        verify(productDao).saveAll(products);
    }
}
