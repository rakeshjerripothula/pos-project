package com.increff.pos.dto;

import com.increff.pos.model.data.TsvUploadResult;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.api.ClientApi;
import com.increff.pos.entity.ClientEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HeaderRequirementTest {

    @Mock
    private ClientApi clientApi;
    
    @InjectMocks
    private ProductDto productDto;
    
    @InjectMocks
    private InventoryDto inventoryDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Mock clientApi to return valid clients for any client ID
        try {
            when(clientApi.getById(any())).thenReturn(new ClientEntity());
        } catch (ApiException e) {
            // Should not happen in setup
        }
    }

    @Test
    void testProductTsv_WithoutHeader() {
        // TSV without header - first row is treated as data
        String tsvContent = "Test Product 1\t10.99\t1\tBAR001\n" +
                "Test Product 2\t20.50\t2\tBAR002\n";
        
        MultipartFile file = new MockMultipartFile(
            "file", 
            "products.tsv", 
            "text/tab-separated-values", 
            tsvContent.getBytes()
        );

        TsvUploadResult<ProductForm> result = productDto.parseProductTsv(file);

        // First row "Test Product 1\t10.99\t1\tBAR001" is skipped as header
        // Only second row is processed as data
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size()); // Only 1 row processed (header skipped)
        assertNull(result.getErrors());
        
        ProductForm form = result.getData().get(0);
        assertEquals("Test Product 2", form.getProductName());
        assertEquals("20.50", form.getMrp().toString()); // This is the second row from original
        assertEquals(2, form.getClientId());
        assertEquals("BAR002", form.getBarcode());
    }

    @Test
    void testInventoryTsv_WithoutHeader() throws IOException {
        // TSV without header - first row is treated as data
        String tsvContent = "1\t100\n" +
                "2\t200\n";
        
        MultipartFile file = new MockMultipartFile(
            "file", 
            "inventory.tsv", 
            "text/tab-separated-values", 
            tsvContent.getBytes()
        );

        TsvUploadResult<InventoryForm> result = inventoryDto.parseInventoryTsv(file);

        // First row "1\t100" is skipped as header
        // Only second row is processed as data
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size()); // Only 1 row processed (header skipped)
        assertNull(result.getErrors());
        
        InventoryForm form = result.getData().get(0);
        assertEquals(2, form.getProductId());
        assertEquals(200, form.getQuantity());
    }

    @Test
    void testProductTsv_WithHeader() throws IOException {
        // TSV with proper header
        String tsvContent = "productName\tmrp\tclientId\tbarcode\n" +
                "Test Product 1\t10.99\t1\tBAR001\n" +
                "Test Product 2\t20.50\t2\tBAR002\n";
        
        MultipartFile file = new MockMultipartFile(
            "file", 
            "products.tsv", 
            "text/tab-separated-values", 
            tsvContent.getBytes()
        );

        TsvUploadResult<ProductForm> result = productDto.parseProductTsv(file);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(2, result.getData().size()); // Both data rows processed
        assertNull(result.getErrors());
    }

    @Test
    void testInventoryTsv_WithHeader() throws IOException {
        // TSV with proper header
        String tsvContent = "productId\tquantity\n" +
                "1\t100\n" +
                "2\t200\n";
        
        MultipartFile file = new MockMultipartFile(
            "file", 
            "inventory.tsv", 
            "text/tab-separated-values", 
            tsvContent.getBytes()
        );

        TsvUploadResult<InventoryForm> result = inventoryDto.parseInventoryTsv(file);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(2, result.getData().size()); // Both data rows processed
        assertNull(result.getErrors());
    }

    @Test
    void testEmptyTsv_WithHeaderOnly() throws IOException {
        // TSV with only header
        String tsvContent = "productId\tquantity\n";
        
        MultipartFile file = new MockMultipartFile(
            "file", 
            "inventory.tsv", 
            "text/tab-separated-values", 
            tsvContent.getBytes()
        );

        TsvUploadResult<InventoryForm> result = inventoryDto.parseInventoryTsv(file);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(0, result.getData().size()); // No data rows
        assertNull(result.getErrors());
    }
}
