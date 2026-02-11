package com.increff.pos.dto;

import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.data.TsvUploadError;
import com.increff.pos.model.data.TsvUploadResult;
import com.increff.pos.model.form.InventoryForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InventoryDtoTest {

    @Mock
    private com.increff.pos.flow.InventoryFlow inventoryFlow;

    @InjectMocks
    private InventoryDto inventoryDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testParseInventoryTsv_WithValidData() throws IOException {
        String tsvContent = """
                productId\tquantity
                1\t100
                2\t200
                """;
        
        MultipartFile file = new MockMultipartFile(
            "file", 
            "inventory.tsv", 
            "text/tab-separated-values", 
            tsvContent.getBytes()
        );

        InventoryDto inventoryDto = new InventoryDto();
        TsvUploadResult<InventoryForm> result = inventoryDto.parseInventoryTsv(file);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(2, result.getData().size());
        assertNull(result.getErrors());
    }

    @Test
    void testParseInventoryTsv_WithInvalidData() throws IOException {
        String tsvContent = """
                productId\tquantity
                invalid_id\t100
                2\t-50
                1\t200
                """;
        
        MultipartFile file = new MockMultipartFile(
            "file", 
            "inventory.tsv", 
            "text/tab-separated-values", 
            tsvContent.getBytes()
        );

        InventoryDto inventoryDto = new InventoryDto();
        TsvUploadResult<InventoryForm> result = inventoryDto.parseInventoryTsv(file);

        assertFalse(result.isSuccess());
        assertNotNull(result.getErrors());
        assertEquals(2, result.getErrors().size());
        assertNull(result.getData());
        
        List<TsvUploadError> errors = result.getErrors();
        
        // Check first error - invalid product ID format
        TsvUploadError error1 = errors.get(0);
        assertEquals(2, error1.getRowNumber());
        assertTrue(error1.getErrorMessage().contains("Invalid product ID format"));
        
        // Check second error - negative quantity
        TsvUploadError error2 = errors.get(1);
        assertEquals(3, error2.getRowNumber());
        assertTrue(error2.getErrorMessage().contains("Quantity cannot be negative"));
    }

    @Test
    void testParseInventoryTsv_WithDuplicateProductIds() throws IOException {
        String tsvContent = """
                productId\tquantity
                1\t100
                2\t200
                1\t300
                """;
        
        MultipartFile file = new MockMultipartFile(
            "file", 
            "inventory.tsv", 
            "text/tab-separated-values", 
            tsvContent.getBytes()
        );

        InventoryDto inventoryDto = new InventoryDto();
        TsvUploadResult<InventoryForm> result = inventoryDto.parseInventoryTsv(file);

        assertFalse(result.isSuccess());
        assertNotNull(result.getErrors());
        assertEquals(1, result.getErrors().size());
        
        TsvUploadError error = result.getErrors().get(0);
        assertEquals(4, error.getRowNumber());
        assertTrue(error.getErrorMessage().contains("Duplicate product ID"));
    }

    @Test
    void testParseInventoryTsv_WithInsufficientColumns() throws IOException {
        String tsvContent = """
                productId\tquantity
                1
                """;
        
        MultipartFile file = new MockMultipartFile(
            "file", 
            "inventory.tsv", 
            "text/tab-separated-values", 
            tsvContent.getBytes()
        );

        InventoryDto inventoryDto = new InventoryDto();
        TsvUploadResult<InventoryForm> result = inventoryDto.parseInventoryTsv(file);

        assertFalse(result.isSuccess());
        assertNotNull(result.getErrors());
        assertEquals(1, result.getErrors().size());
        
        TsvUploadError error = result.getErrors().get(0);
        assertEquals(2, error.getRowNumber());
        assertTrue(error.getErrorMessage().contains("Expected at least 2 columns"));
    }

    @Test
    void testParseInventoryTsv_WithZeroProductId() throws IOException {
        String tsvContent = """
                productId\tquantity
                0\t100
                """;
        
        MultipartFile file = new MockMultipartFile(
            "file", 
            "inventory.tsv", 
            "text/tab-separated-values", 
            tsvContent.getBytes()
        );

        InventoryDto inventoryDto = new InventoryDto();
        TsvUploadResult<InventoryForm> result = inventoryDto.parseInventoryTsv(file);

        assertFalse(result.isSuccess());
        assertNotNull(result.getErrors());
        assertEquals(1, result.getErrors().size());
        
        TsvUploadError error = result.getErrors().getFirst();
        assertEquals(2, error.getRowNumber());
        assertTrue(error.getErrorMessage().contains("Product ID must be greater than 0"));
    }

    @Test
    void upsert_validForm_success() {
        // Arrange
        InventoryForm form = new InventoryForm();
        form.setProductId(1);
        form.setQuantity(100);
        
        com.increff.pos.model.data.InventoryData expectedData = new com.increff.pos.model.data.InventoryData();
        expectedData.setProductId(1);
        expectedData.setQuantity(100);
        expectedData.setProductName("Test Product");
        
        when(inventoryFlow.upsert(any())).thenReturn(expectedData);

        // Act
        InventoryData result = inventoryDto.upsert(form);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals(100, result.getQuantity());
        assertEquals("Test Product", result.getProductName());
        verify(inventoryFlow, times(1)).upsert(any());
    }

    @Test
    void upsert_invalidProductId_throwsException() {
        // Arrange
        InventoryForm form = new InventoryForm();
        form.setProductId(null); // Invalid: null
        form.setQuantity(100);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> inventoryDto.upsert(form));
        assertEquals("BAD_DATA", exception.getStatus().name());
        assertTrue(exception.hasErrors());
        assertTrue(exception.getErrors().get(0).getMessage().contains("Product ID is required"));
        verify(inventoryFlow, never()).upsert(any());
    }

    @Test
    void upsert_nullForm_throwsException() {
        // Act & Assert
        assertThrows(ApiException.class, () -> inventoryDto.upsert(null));
        verify(inventoryFlow, never()).upsert(any());
    }

    @Test
    void bulkUpsert_validForms_success() {
        // Arrange
        List<InventoryForm> forms = Arrays.asList(
            createInventoryForm(1, 100),
            createInventoryForm(2, 200)
        );
        
        List<com.increff.pos.model.data.InventoryData> expectedData = Arrays.asList(
            createInventoryData(1, 100, "Product 1"),
            createInventoryData(2, 200, "Product 2")
        );
        
        when(inventoryFlow.bulkUpsertAndGetData(any())).thenReturn(expectedData);

        // Act
        List<InventoryData> result = inventoryDto.bulkUpsert(forms);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getProductId());
        assertEquals(100, result.get(0).getQuantity());
        assertEquals("Product 1", result.get(0).getProductName());
        verify(inventoryFlow, times(1)).bulkUpsertAndGetData(any());
    }

    @Test
    void bulkUpsert_duplicateProductId_throwsException() {
        // Arrange
        List<InventoryForm> forms = Arrays.asList(
            createInventoryForm(1, 100),
            createInventoryForm(1, 200) // Duplicate product ID
        );

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> inventoryDto.bulkUpsert(forms));
        assertEquals("BAD_DATA", exception.getStatus().name());
        assertTrue(exception.getMessage().contains("Duplicate productId"));
        verify(inventoryFlow, never()).bulkUpsertAndGetData(any());
    }

    private com.increff.pos.entity.InventoryEntity createInventoryEntity(Integer productId, Integer quantity) {
        com.increff.pos.entity.InventoryEntity inventory = new com.increff.pos.entity.InventoryEntity();
        inventory.setProductId(productId);
        inventory.setQuantity(quantity);
        return inventory;
    }

    private com.increff.pos.entity.ProductEntity createProductEntity(Integer id, String name) {
        com.increff.pos.entity.ProductEntity product = new com.increff.pos.entity.ProductEntity();
        product.setId(id);
        product.setProductName(name);
        return product;
    }

    private com.increff.pos.model.data.InventoryData createInventoryData(Integer productId, Integer quantity, String productName) {
        com.increff.pos.model.data.InventoryData data = new com.increff.pos.model.data.InventoryData();
        data.setProductId(productId);
        data.setQuantity(quantity);
        data.setProductName(productName);
        return data;
    }

    private InventoryForm createInventoryForm(Integer productId, Integer quantity) {
        InventoryForm form = new InventoryForm();
        form.setProductId(productId);
        form.setQuantity(quantity);
        return form;
    }
}
