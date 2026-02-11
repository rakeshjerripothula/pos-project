package com.increff.pos.dto;

import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import com.increff.pos.model.data.FieldErrorData;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.form.InventoryForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InventoryDtoBusinessTest {

    @Mock
    private com.increff.pos.flow.InventoryFlow inventoryFlow;

    @InjectMocks
    private InventoryDto inventoryDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
        form.setProductId(null); // invalid
        form.setQuantity(100);

        // Act
        ApiException exception = assertThrows(ApiException.class, () -> inventoryDto.upsert(form));

        // Assert: top-level exception
        assertEquals(ApiStatus.BAD_DATA, exception.getStatus());
        assertEquals("Input validation failed", exception.getMessage());

        // Assert: field-level validation errors
        assertNotNull(exception.getErrors());
        assertEquals(1, exception.getErrors().size());

        FieldErrorData error = exception.getErrors().getFirst();
        assertEquals("productId", error.getField());
        assertEquals("Product ID is required", error.getMessage());

        // Assert: flow is never called
        verify(inventoryFlow, never()).upsert(any());
    }

    @Test
    void upsert_invalidQuantity_throwsException() {
        // Arrange
        InventoryForm form = new InventoryForm();
        form.setProductId(1);
        form.setQuantity(-1); // Invalid: negative

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> inventoryDto.upsert(form));
        assertEquals("BAD_DATA", exception.getStatus().name());
        assertTrue(exception.hasErrors());
        verify(inventoryFlow, never()).upsert(any());
    }

    @Test
    void upsert_nullForm_throwsException() {
        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> inventoryDto.upsert(null));
        assertEquals("BAD_DATA", exception.getStatus().name());
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
        assertEquals(2, result.get(1).getProductId());
        assertEquals(200, result.get(1).getQuantity());
        assertEquals("Product 2", result.get(1).getProductName());
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

    @Test
    void bulkUpsert_invalidForm_throwsException() {
        // Arrange
        List<InventoryForm> forms = Arrays.asList(
            createInventoryForm(null, 100) // Invalid: null product ID
        );

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> inventoryDto.bulkUpsert(forms));
        assertEquals("BAD_DATA", exception.getStatus().name());
        assertTrue(exception.hasErrors());
        verify(inventoryFlow, never()).bulkUpsertAndGetData(any());
    }

    @Test
    void getAll_success() {
        // Arrange
        List<com.increff.pos.model.data.InventoryData> expectedData = Arrays.asList(
            createInventoryData(1, 100, "Product 1"),
            createInventoryData(2, 200, "Product 2")
        );
        
        when(inventoryFlow.listAllForEnabledClientsWithData()).thenReturn(expectedData);

        // Act
        List<InventoryData> result = inventoryDto.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Product 1", result.get(0).getProductName());
        assertEquals("Product 2", result.get(1).getProductName());
        verify(inventoryFlow, times(1)).listAllForEnabledClientsWithData();
    }

    @Test
    void getAll_emptyList_success() {
        // Arrange
        when(inventoryFlow.listAllForEnabledClientsWithData()).thenReturn(Arrays.asList());

        // Act
        List<InventoryData> result = inventoryDto.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(inventoryFlow, times(1)).listAllForEnabledClientsWithData();
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
