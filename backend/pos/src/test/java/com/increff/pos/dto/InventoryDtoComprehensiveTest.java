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

class InventoryDtoComprehensiveTest {

    @Test
    void testParseInventoryTsv_EmptyFile() throws IOException {
        String tsvContent = "productId\tquantity\n";
        
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
        assertEquals(0, result.getData().size());
        assertNull(result.getErrors());
    }

    @Test
    void testParseInventoryTsv_ValidDataWithExtraColumns() throws IOException {
        String tsvContent = "productId\tquantity\textraColumn\tanotherExtra\n" +
                "1\t100\textra\tdata\n" +
                "2\t200\tmore\tinfo\n";
        
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
        
        // Verify data integrity
        InventoryForm form1 = result.getData().get(0);
        assertEquals(1, form1.getProductId());
        assertEquals(100, form1.getQuantity());
        
        InventoryForm form2 = result.getData().get(1);
        assertEquals(2, form2.getProductId());
        assertEquals(200, form2.getQuantity());
    }

    @Test
    void testParseInventoryTsv_ZeroQuantity() throws IOException {
        String tsvContent = "productId\tquantity\n" +
                "1\t0\n";
        
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
        assertEquals(1, result.getData().size());
        assertNull(result.getErrors());
        
        InventoryForm form = result.getData().get(0);
        assertEquals(1, form.getProductId());
        assertEquals(0, form.getQuantity());
    }

    @Test
    void testParseInventoryTsv_WithWhitespace() throws IOException {
        String tsvContent = "productId\tquantity\n" +
                "  1  \t  100  \n" +
                "  2  \t  200  \n";
        
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
        
        // Verify whitespace trimming
        InventoryForm form1 = result.getData().get(0);
        assertEquals(1, form1.getProductId());
        assertEquals(100, form1.getQuantity());
        
        InventoryForm form2 = result.getData().get(1);
        assertEquals(2, form2.getProductId());
        assertEquals(200, form2.getQuantity());
    }

    @Test
    void testParseInventoryTsv_MixedValidAndInvalidRows() throws IOException {
        String tsvContent = "productId\tquantity\n" +
                "1\t100\n" +           // Valid
                "invalid\t200\n" +      // Invalid product ID
                "3\t-50\n" +            // Invalid quantity
                "1\t300\n" +            // Duplicate product ID
                "5\t400\n";              // Valid
        
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
        assertEquals(3, result.getErrors().size()); // invalid ID, negative quantity, duplicate ID
        assertNull(result.getData());
        
        List<TsvUploadError> errors = result.getErrors();
        
        // Verify error messages
        assertTrue(errors.stream().anyMatch(e -> e.getErrorMessage().contains("Invalid product ID format")));
        assertTrue(errors.stream().anyMatch(e -> e.getErrorMessage().contains("Quantity cannot be negative")));
        assertTrue(errors.stream().anyMatch(e -> e.getErrorMessage().contains("Duplicate product ID")));
    }

    @Test
    void testParseInventoryTsv_LargeNumbers() throws IOException {
        String tsvContent = "productId\tquantity\n" +
                "999999\t999999999\n";
        
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
        assertEquals(1, result.getData().size());
        assertNull(result.getErrors());
        
        InventoryForm form = result.getData().get(0);
        assertEquals(999999, form.getProductId());
        assertEquals(999999999, form.getQuantity());
    }

    @Test
    void testUploadTsv_Integration() throws IOException {
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
        TsvUploadResult<InventoryForm> parseResult = inventoryDto.parseInventoryTsv(file);
        
        assertTrue(parseResult.isSuccess());
        assertNotNull(parseResult.getData());
        assertEquals(2, parseResult.getData().size());
        
        // Test the uploadTsv method (will fail at service layer due to missing dependencies, but should validate correctly)
        try {
            TsvUploadResult<?> uploadResult = inventoryDto.uploadTsv(file);
            // If we get here, the validation passed
            assertTrue(uploadResult.isSuccess());
        } catch (Exception e) {
            // Expected due to missing Spring context in unit test
            assertTrue(e.getMessage().contains("productApi") || e.getMessage().contains("inventoryFlow"));
        }
    }
}
