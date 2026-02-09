package com.increff.pos.dto;

import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import com.increff.pos.model.data.FieldErrorData;
import com.increff.pos.model.data.ProductData;
import com.increff.pos.model.form.ProductForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductDtoComprehensiveTest {

    @Mock
    private com.increff.pos.api.ProductApi productApi;

    @InjectMocks
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createProduct_validForm_success() {
        // Arrange
        ProductForm form = new ProductForm();
        form.setProductName("Test Product");
        form.setMrp(new BigDecimal("10.99"));
        form.setClientId(1);
        form.setBarcode("BAR001");
        
        com.increff.pos.entity.ProductEntity savedProduct = createProductEntity(1, "Test Product", new BigDecimal("10.99"), 1, "BAR001");
        when(productApi.createProduct(any())).thenReturn(savedProduct);

        // Act
        ProductData result = productDto.createProduct(form);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Test Product", result.getProductName());
        assertEquals(new BigDecimal("10.99"), result.getMrp());
        assertEquals(1, result.getClientId());
        assertEquals("BAR001", result.getBarcode());
        verify(productApi, times(1)).createProduct(any());
    }

    @Test
    void createProduct_invalidMrp_throwsException() {
        // Arrange
        ProductForm form = new ProductForm();
        form.setProductName("Test Product");
        form.setMrp(BigDecimal.ZERO); // invalid
        form.setClientId(1);
        form.setBarcode("BAR001");

        // Act
        ApiException exception = assertThrows(ApiException.class, () -> productDto.createProduct(form));

        // Assert: top-level exception
        assertEquals(ApiStatus.BAD_DATA, exception.getStatus());
        assertEquals("Input validation failed", exception.getMessage());

        // Assert: field-level validation error
        assertNotNull(exception.getErrors());
        assertEquals(1, exception.getErrors().size());

        FieldErrorData error = exception.getErrors().getFirst();
        assertEquals("mrp", error.getField());
        assertEquals("MRP must be greater than 0", error.getMessage());

        // Assert: API not invoked
        verify(productApi, never()).createProduct(any());
    }

    @Test
    void createProduct_nullForm_throwsException() {
        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> productDto.createProduct(null));
        assertEquals("BAD_DATA", exception.getStatus().name());
        verify(productApi, never()).createProduct(any());
    }

    @Test
    void updateProduct_validForm_success() {
        // Arrange
        Integer productId = 1;
        ProductForm form = new ProductForm();
        form.setProductName("Updated Product");
        form.setMrp(new BigDecimal("15.99"));
        form.setClientId(2);
        form.setBarcode("BAR002");
        
        com.increff.pos.entity.ProductEntity updatedProduct = createProductEntity(productId, "Updated Product", new BigDecimal("15.99"), 2, "BAR002");
        when(productApi.updateProduct(eq(productId), any())).thenReturn(updatedProduct);

        // Act
        ProductData result = productDto.updateProduct(productId, form);

        // Assert
        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals("Updated Product", result.getProductName());
        assertEquals(new BigDecimal("15.99"), result.getMrp());
        verify(productApi, times(1)).updateProduct(eq(productId), any());
    }

    @Test
    void updateProduct_invalidProductId_throwsException() {
        // Arrange
        ProductForm form = new ProductForm();
        form.setProductName("Test Product");
        form.setMrp(new BigDecimal("10.99"));
        form.setClientId(1);
        form.setBarcode("BAR001");

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> productDto.updateProduct(null, form));
        assertEquals("BAD_DATA", exception.getStatus().name());
        assertTrue(exception.getMessage().contains("Product ID is required"));
        verify(productApi, never()).updateProduct(any(), any());
    }

    @Test
    void updateProduct_invalidForm_throwsException() {
        // Arrange
        Integer productId = 1;
        ProductForm form = new ProductForm();
        form.setProductName(""); // invalid
        form.setMrp(new BigDecimal("10.99"));
        form.setClientId(1);
        form.setBarcode("BAR001");

        // Act
        ApiException exception = assertThrows(
                ApiException.class,
                () -> productDto.updateProduct(productId, form)
        );

        // Assert: top-level exception
        assertEquals(ApiStatus.BAD_DATA, exception.getStatus());
        assertEquals("Input validation failed", exception.getMessage());

        // Assert: field-level validation errors
        assertNotNull(exception.getErrors());
        assertEquals(1, exception.getErrors().size());

        FieldErrorData error = exception.getErrors().get(0);
        assertEquals("productName", error.getField());
        assertEquals("Product name is required", error.getMessage());

        // Assert: API not invoked
        verify(productApi, never()).updateProduct(any(), any());
    }


    @Test
    void bulkCreateProducts_validForms_success() {
        // Arrange
        List<ProductForm> forms = Arrays.asList(
            createProductForm("Product 1", new BigDecimal("10.99"), 1, "BAR001"),
            createProductForm("Product 2", new BigDecimal("15.99"), 2, "BAR002")
        );
        
        List<com.increff.pos.entity.ProductEntity> savedProducts = Arrays.asList(
            createProductEntity(1, "Product 1", new BigDecimal("10.99"), 1, "BAR001"),
            createProductEntity(2, "Product 2", new BigDecimal("15.99"), 2, "BAR002")
        );
        
        when(productApi.bulkCreateProducts(any())).thenReturn(savedProducts);

        // Act
        List<ProductData> result = productDto.bulkCreateProducts(forms);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Product 1", result.get(0).getProductName());
        assertEquals("Product 2", result.get(1).getProductName());
        verify(productApi, times(1)).bulkCreateProducts(any());
    }

    @Test
    void bulkCreateProducts_invalidForm_throwsException() {
        // Arrange
        List<ProductForm> forms = List.of(
                createProductForm("", new BigDecimal("10.99"), 1, "BAR001")
        );

        // Act
        ApiException exception = assertThrows(
                ApiException.class,
                () -> productDto.bulkCreateProducts(forms)
        );

        // Assert: top-level exception
        assertEquals(ApiStatus.BAD_DATA, exception.getStatus());
        assertEquals("Input validation failed", exception.getMessage());

        // Assert: field-level validation error
        assertNotNull(exception.getErrors());
        assertEquals(1, exception.getErrors().size());

        FieldErrorData error = exception.getErrors().get(0);
        assertEquals("productName", error.getField());
        assertEquals("Product name is required", error.getMessage());

        // Assert: API not invoked
        verify(productApi, never()).bulkCreateProducts(any());
    }


    @Test
    void getById_validId_success() {
        // Arrange
        Integer productId = 1;
        com.increff.pos.entity.ProductEntity product = createProductEntity(productId, "Test Product", new BigDecimal("10.99"), 1, "BAR001");
        when(productApi.getProductById(productId)).thenReturn(product);

        // Act
        ProductData result = productDto.getById(productId);

        // Assert
        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals("Test Product", result.getProductName());
        verify(productApi, times(1)).getProductById(productId);
    }

    @Test
    void getById_invalidId_throwsException() {
        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> productDto.getById(null));
        assertEquals("BAD_DATA", exception.getStatus().name());
        assertTrue(exception.getMessage().contains("Product ID is required"));
        verify(productApi, never()).getProductById(any());
    }

    @Test
    void getAll_success() {
        // Arrange
        List<com.increff.pos.entity.ProductEntity> products = Arrays.asList(
            createProductEntity(1, "Product 1", new BigDecimal("10.99"), 1, "BAR001"),
            createProductEntity(2, "Product 2", new BigDecimal("15.99"), 2, "BAR002")
        );
        when(productApi.getAll()).thenReturn(products);

        // Act
        List<ProductData> result = productDto.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Product 1", result.get(0).getProductName());
        assertEquals("Product 2", result.get(1).getProductName());
        verify(productApi, times(1)).getAll();
    }

    private com.increff.pos.entity.ProductEntity createProductEntity(Integer id, String name, BigDecimal mrp, Integer clientId, String barcode) {
        com.increff.pos.entity.ProductEntity product = new com.increff.pos.entity.ProductEntity();
        product.setId(id);
        product.setProductName(name);
        product.setMrp(mrp);
        product.setClientId(clientId);
        product.setBarcode(barcode);
        return product;
    }

    private ProductForm createProductForm(String name, BigDecimal mrp, Integer clientId, String barcode) {
        ProductForm form = new ProductForm();
        form.setProductName(name);
        form.setMrp(mrp);
        form.setClientId(clientId);
        form.setBarcode(barcode);
        return form;
    }
}
