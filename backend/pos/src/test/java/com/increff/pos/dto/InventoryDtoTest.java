package com.increff.pos.dto;

import com.increff.pos.model.data.TsvUploadError;
import com.increff.pos.model.data.TsvUploadResult;
import com.increff.pos.model.form.InventoryForm;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryDtoTest {

    @Test
    void testParseInventoryTsv_WithValidData() throws IOException {
        String tsvContent = "productId\tquantity\n" +
                "1\t100\n" +
                "2\t200\n";
        
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
        String tsvContent = "productId\tquantity\n" +
                "invalid_id\t100\n" +
                "2\t-50\n" +
                "1\t200\n"; // Duplicate product ID
        
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
        String tsvContent = "productId\tquantity\n" +
                "1\t100\n" +
                "2\t200\n" +
                "1\t300\n"; // Duplicate product ID
        
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
        String tsvContent = "productId\tquantity\n" +
                "1\n"; // Missing quantity
        
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
        String tsvContent = "productId\tquantity\n" +
                "0\t100\n";
        
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
        assertTrue(error.getErrorMessage().contains("Product ID must be greater than 0"));
    }
}
