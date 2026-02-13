package com.increff.pos.dto;

import com.increff.pos.entity.ClientEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.PagedResponse;
import com.increff.pos.model.data.ProductData;
import com.increff.pos.model.data.TsvUploadError;
import com.increff.pos.model.data.TsvUploadResult;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.model.form.ProductSearchForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ProductDtoTest {

    @Mock
    private com.increff.pos.api.ProductApi productApi;

    @Mock
    private com.increff.pos.api.ClientApi clientApi;

    @InjectMocks
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testParseProductTsv_WithValidData() {
        // Mock client existence checks
        when(clientApi.getById(1)).thenReturn(new ClientEntity());
        when(clientApi.getById(2)).thenReturn(new ClientEntity());
        
        String tsvContent = """
                productName\tmrp\tclientId\tbarcode
                Test Product 1\t10.99\t1\tBAR001
                Test Product 2\t20.50\t2\tBAR002
                """;
        
        MultipartFile file = new MockMultipartFile(
            "file", 
            "products.tsv", 
            "text/tab-separated-values", 
            tsvContent.getBytes()
        );

        TsvUploadResult<ProductForm> result = productDto.parseProductTsv(file);

        System.out.println("Result success: " + result.isSuccess());
        if (!result.isSuccess()) {
            System.out.println("Errors: " + result.getErrors());
            for (TsvUploadError error : result.getErrors()) {
                System.out.println("Error: " + error.getErrorMessage());
            }
        }

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(2, result.getData().size());
        assertNull(result.getErrors());
    }

    @Test
    void testParseProductTsv_WithInvalidData(){
        String tsvContent = """
                productName\tmrp\tclientId\tbarcode
                Test Product 1\tinvalid_mrp\t1\tBAR001
                \t20.50\t2\tBAR002
                Test Product 3\t-5.00\t3\tBAR003
                """;
        
        MultipartFile file = new MockMultipartFile(
            "file", 
            "products.tsv", 
            "text/tab-separated-values", 
            tsvContent.getBytes()
        );

        ProductDto productDto = new ProductDto();
        TsvUploadResult<ProductForm> result = productDto.parseProductTsv(file);

        assertFalse(result.isSuccess());
        assertNotNull(result.getErrors());
        assertEquals(3, result.getErrors().size());
        assertNull(result.getData());
        
        List<TsvUploadError> errors = result.getErrors();
        
        // Check first error - invalid MRP format
        TsvUploadError error1 = errors.get(0);
        assertEquals(2, error1.getRowNumber());
        assertTrue(error1.getErrorMessage().contains("Invalid MRP format"));
        
        // Check second error - empty product name
        TsvUploadError error2 = errors.get(1);
        assertEquals(3, error2.getRowNumber());
        assertTrue(error2.getErrorMessage().contains("Product name is required"));
        
        // Check third error - negative MRP
        TsvUploadError error3 = errors.get(2);
        assertEquals(4, error3.getRowNumber());
        assertTrue(error3.getErrorMessage().contains("MRP must be greater than 0"));
    }

    @Test
    void testParseProductTsv_WithInsufficientColumns() throws IOException {
        String tsvContent = "productName\tmrp\tclientId\tbarcode\n" +
                "Test Product 1\t10.99\t1\n"; // Missing barcode
        
        MultipartFile file = new MockMultipartFile(
            "file", 
            "products.tsv", 
            "text/tab-separated-values", 
            tsvContent.getBytes()
        );

        ProductDto productDto = new ProductDto();
        TsvUploadResult<ProductForm> result = productDto.parseProductTsv(file);

        assertFalse(result.isSuccess());
        assertNotNull(result.getErrors());
        assertEquals(1, result.getErrors().size());
        
        TsvUploadError error = result.getErrors().getFirst();
        assertEquals(2, error.getRowNumber());
        assertTrue(error.getErrorMessage().contains("Expected at least 4 columns"));
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
        form.setMrp(new BigDecimal("-1.00")); // Invalid MRP (negative)
        form.setClientId(1);
        form.setBarcode("BAR001");

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> productDto.createProduct(form));
        assertEquals("BAD_DATA", exception.getStatus().name());
        assertTrue(exception.hasErrors());
        assertTrue(exception.getErrors().getFirst().getMessage().contains("MRP must be greater than 0"));
        verify(productApi, never()).createProduct(any());
    }

    @Test
    void createProduct_nullForm_throwsException() {
        // Act & Assert
        assertThrows(ApiException.class, () -> productDto.createProduct(null));
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
                createProductForm("", new BigDecimal("10.99"), 1, "BAR001") // Invalid: empty name
        );

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> productDto.bulkCreateProducts(forms));
        assertEquals("BAD_DATA", exception.getStatus().name());
        assertTrue(exception.hasErrors());
        assertTrue(exception.getErrors().getFirst().getMessage().contains("Product name is required"));
        verify(productApi, never()).bulkCreateProducts(any());
    }

    @Test
    void testGetAll() {
        // Arrange
        List<com.increff.pos.entity.ProductEntity> entities = Arrays.asList(
                createProductEntity(1, "Product 1", new BigDecimal("10.99"), 1, "BAR001"),
                createProductEntity(2, "Product 2", new BigDecimal("15.99"), 2, "BAR002")
        );

        when(productApi.getAll()).thenReturn(entities);

        // Act
        List<ProductData> result = productDto.getAll();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Product 1", result.get(0).getProductName());
        assertEquals("Product 2", result.get(1).getProductName());
        verify(productApi).getAll();
    }

    @Test
    void testListProducts() {
        // Arrange
        ProductSearchForm form = new ProductSearchForm();
        form.setPage(0);
        form.setPageSize(10);
        form.setClientId(1);
        form.setBarcode("BAR001");
        form.setProductName("Test Product");

        com.increff.pos.entity.ProductEntity entity = createProductEntity(1, "Test Product", new BigDecimal("10.99"), 1, "BAR001");
        Page<com.increff.pos.entity.ProductEntity> page = new PageImpl<>(List.of(entity));

        when(productApi.searchProducts(any(), any(), any(), any())).thenReturn(page);

        // Act
        PagedResponse<ProductData> result = productDto.listProducts(form);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(1, result.getTotal());
        assertEquals("Test Product", result.getData().get(0).getProductName());
        verify(productApi).searchProducts(any(), any(), any(), any());
    }

    @Test
    void testUploadProductsTsv() {
        // Arrange
        ProductForm form = new ProductForm();
        form.setProductName("Test Product");
        form.setMrp(BigDecimal.valueOf(100.0));
        form.setClientId(1);
        form.setBarcode("BAR001");

        TsvUploadResult<ProductForm> parseResult = TsvUploadResult.success(List.of(form));
        when(productApi.bulkCreateProducts(any())).thenReturn(List.of(createProductEntity(1, "Test Product", BigDecimal.valueOf(100.0), 1, "BAR001")));

        // Mock parseProductTsv method by using spy
        ProductDto spyProductDto = spy(productDto);
        doReturn(parseResult).when(spyProductDto).parseProductTsv(any());

        String tsvContent = "productName\tmrp\tclientId\tbarcode\nTest Product\t100.00\t1\tBAR001";
        MockMultipartFile file = new MockMultipartFile(
                "file", "products.tsv", "text/tab-separated-values", tsvContent.getBytes()
        );

        // Act
        TsvUploadResult<ProductData> result = spyProductDto.uploadProductsTsv(file);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        assertEquals("Test Product", result.getData().get(0).getProductName());
    }

    @Test
    void testUploadProductsTsv_emptyFile_throwsException() {
        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> productDto.uploadProductsTsv(null));
        assertEquals("BAD_DATA", exception.getStatus().name());
        assertTrue(exception.getMessage().contains("Empty file"));
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
