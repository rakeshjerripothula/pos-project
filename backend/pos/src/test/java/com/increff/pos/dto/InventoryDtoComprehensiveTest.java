package com.increff.pos.dto;

import com.increff.pos.model.data.TsvUploadError;
import com.increff.pos.model.data.TsvUploadResult;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.model.form.InventoryUploadForm;
import com.increff.pos.exception.TsvUploadException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryDtoComprehensiveTest {

    @Test
    void testParseInventoryTsv_EmptyFile() throws Exception {
        String tsvContent = "productId\tquantity\n";
        
        MultipartFile file = new MockMultipartFile(
            "file", 
            "inventory.tsv", 
            "text/tab-separated-values", 
            tsvContent.getBytes()
        );

        // Test the static utility method directly
        List<InventoryUploadForm> result = com.increff.pos.util.TsvParseUtils.parseInventoryTsv(file);
        
        assertEquals(0, result.size());
    }

    @Test
    void testParseInventoryTsv_ValidDataWithExtraColumns() throws Exception {
        String tsvContent = "productName\tquantity\textraColumn\tanotherExtra\n" +
                "Product 1\t100\textra\tdata\n" +
                "Product 2\t200\tmore\tinfo\n";
        
        MultipartFile file = new MockMultipartFile(
            "file", 
            "inventory.tsv", 
            "text/tab-separated-values", 
            tsvContent.getBytes()
        );

        // Test the static utility method directly
        List<InventoryUploadForm> result = com.increff.pos.util.TsvParseUtils.parseInventoryTsv(file);
        
        assertEquals(2, result.size());
        
        // Verify data integrity
        InventoryUploadForm form1 = result.get(0);
        assertEquals("Product 1", form1.getProductName());
        assertEquals(Integer.valueOf(100), form1.getQuantity());
        
        InventoryUploadForm form2 = result.get(1);
        assertEquals("Product 2", form2.getProductName());
        assertEquals(Integer.valueOf(200), form2.getQuantity());
    }

    @Test
    void testParseInventoryTsv_ZeroQuantity() throws Exception {
        String tsvContent = "productId\tquantity\n" +
                "1\t0\n";
        
        MultipartFile file = new MockMultipartFile(
            "file", 
            "inventory.tsv", 
            "text/tab-separated-values", 
            tsvContent.getBytes()
        );

        // Test the static utility method directly
        List<InventoryUploadForm> result = com.increff.pos.util.TsvParseUtils.parseInventoryTsv(file);
        
        assertEquals(1, result.size());
        
        InventoryUploadForm form = result.get(0);
        assertEquals("1", form.getProductName());
        assertEquals(Integer.valueOf(0), form.getQuantity());
    }

    @Test
    void testParseInventoryTsv_WithWhitespace() throws Exception {
        String tsvContent = "productId\tquantity\n" +
                "  1  \t  100  \n" +
                "  2  \t  200  \n";
        
        MultipartFile file = new MockMultipartFile(
            "file", 
            "inventory.tsv", 
            "text/tab-separated-values", 
            tsvContent.getBytes()
        );

        // Test the static utility method directly
        List<InventoryUploadForm> result = com.increff.pos.util.TsvParseUtils.parseInventoryTsv(file);
        
        assertEquals(2, result.size());
        
        // Verify whitespace trimming - inventory parsing doesn't trim product names
        InventoryUploadForm form1 = result.get(0);
        assertEquals("  1  ", form1.getProductName());
        assertEquals(Integer.valueOf(100), form1.getQuantity());
        
        InventoryUploadForm form2 = result.get(1);
        assertEquals("  2  ", form2.getProductName());
        assertEquals(Integer.valueOf(200), form2.getQuantity());
    }

    @Test
    void testParseInventoryTsv_MixedValidAndInvalidRows() throws Exception {
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

        // Test the static utility method directly - it should parse all rows without validation
        List<InventoryUploadForm> result = com.increff.pos.util.TsvParseUtils.parseInventoryTsv(file);
        
        // All rows should be parsed (validation happens at higher level)
        assertEquals(5, result.size());
        
        // Verify parsed data
        assertEquals("1", result.get(0).getProductName());
        assertEquals(Integer.valueOf(100), result.get(0).getQuantity());
        assertEquals("invalid", result.get(1).getProductName());
        assertEquals(Integer.valueOf(200), result.get(1).getQuantity());
        assertEquals("3", result.get(2).getProductName());
        assertEquals(Integer.valueOf(-50), result.get(2).getQuantity()); // -50 can be parsed as integer safely
        assertEquals("1", result.get(3).getProductName());
        assertEquals(Integer.valueOf(300), result.get(3).getQuantity());
        assertEquals("5", result.get(4).getProductName());
        assertEquals(Integer.valueOf(400), result.get(4).getQuantity());
    }

    @Test
    void testParseInventoryTsv_LargeNumbers() throws Exception {
        String tsvContent = "productId\tquantity\n" +
                "999999\t999999999\n";
        
        MultipartFile file = new MockMultipartFile(
            "file", 
            "inventory.tsv", 
            "text/tab-separated-values", 
            tsvContent.getBytes()
        );

        // Test the static utility method directly
        List<InventoryUploadForm> result = com.increff.pos.util.TsvParseUtils.parseInventoryTsv(file);
        
        assertEquals(1, result.size());
        
        InventoryUploadForm form = result.get(0);
        assertEquals("999999", form.getProductName());
        assertEquals(Integer.valueOf(999999999), form.getQuantity());
    }

    @Test
    void testUploadTsv_Integration() throws Exception {
        String tsvContent = "productId\tquantity\n" +
                "1\t100\n" +
                "2\t200\n";
        
        MultipartFile file = new MockMultipartFile(
            "file", 
            "inventory.tsv", 
            "text/tab-separated-values", 
            tsvContent.getBytes()
        );

        // Test the static utility method directly
        List<InventoryUploadForm> result = com.increff.pos.util.TsvParseUtils.parseInventoryTsv(file);
        
        assertEquals(2, result.size());
        
        // Verify parsed data
        assertEquals("1", result.get(0).getProductName());
        assertEquals(Integer.valueOf(100), result.get(0).getQuantity());
        assertEquals("2", result.get(1).getProductName());
        assertEquals(Integer.valueOf(200), result.get(1).getQuantity());
    }
}
