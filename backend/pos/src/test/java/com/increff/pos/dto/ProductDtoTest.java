package com.increff.pos.dto;

import com.increff.pos.model.data.TsvUploadError;
import com.increff.pos.model.data.TsvUploadResult;
import com.increff.pos.model.form.ProductForm;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductDtoTest {

    @Test
    void testParseProductTsv_WithValidData() throws IOException {
        String tsvContent = "productName\tmrp\tclientId\tbarcode\n" +
                "Test Product 1\t10.99\t1\tBAR001\n" +
                "Test Product 2\t20.50\t2\tBAR002\n";
        
        MultipartFile file = new MockMultipartFile(
            "file", 
            "products.tsv", 
            "text/tab-separated-values", 
            tsvContent.getBytes()
        );

        ProductDto productDto = new ProductDto();
        TsvUploadResult<ProductForm> result = productDto.parseProductTsv(file);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(2, result.getData().size());
        assertNull(result.getErrors());
    }

    @Test
    void testParseProductTsv_WithInvalidData() throws IOException {
        String tsvContent = "productName\tmrp\tclientId\tbarcode\n" +
                "Test Product 1\tinvalid_mrp\t1\tBAR001\n" +
                "\t20.50\t2\tBAR002\n" +
                "Test Product 3\t-5.00\t3\tBAR003\n";
        
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
        
        TsvUploadError error = result.getErrors().get(0);
        assertEquals(2, error.getRowNumber());
        assertTrue(error.getErrorMessage().contains("Expected at least 4 columns"));
    }
}
