package com.increff.pos.dto;

import com.increff.pos.model.form.ProductUploadForm;
import com.increff.pos.model.form.InventoryUploadForm;
import com.increff.pos.api.ClientApi;
import com.increff.pos.flow.ProductFlow;
import com.increff.pos.flow.InventoryFlow;
import com.increff.pos.entity.ClientEntity;
import com.increff.pos.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HeaderRequirementTest {

    @Mock
    private ClientApi clientApi;
    
    @Mock
    private ProductFlow productFlow;
    
    @Mock
    private InventoryFlow inventoryFlow;
    
    @InjectMocks
    private ProductDto productDto;
    
    @InjectMocks
    private InventoryDto inventoryDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Mock clientApi to return valid clients for any client ID
        try {
            when(clientApi.getCheckById(any())).thenReturn(new ClientEntity());
        } catch (ApiException e) {
            // Should not happen in setup
        }
        
        // Mock productFlow to return client IDs for names
        when(clientApi.getClientIdByName("Client 1")).thenReturn(1);
        when(clientApi.getClientIdByName("Client 2")).thenReturn(2);
    }

    @Test
    void testProductTsv_WithoutHeader() throws Exception {
        // TSV without header - first row is treated as data
        String tsvContent = "Test Product 1\t10.99\tClient 1\tBAR001\n" +
                "Test Product 2\t20.50\tClient 2\tBAR002\n";
        
        MultipartFile file = new MockMultipartFile(
            "file", 
            "products.tsv", 
            "text/tab-separated-values", 
            tsvContent.getBytes()
        );

        // Test the static utility method directly
        List<ProductUploadForm> result = com.increff.pos.util.TsvParseUtils.parseProductTsv(file);
        
        // First row is skipped as header, only second row processed
        assertEquals(1, result.size()); // Only 1 row processed (header skipped)
        
        ProductUploadForm form = result.get(0);
        assertEquals("test product 2", form.getProductName());
        assertEquals("20.50", form.getMrp().toString());
        assertEquals("client 2", form.getClientName());
        assertEquals("bar002", form.getBarcode());
    }

    @Test
    void testInventoryTsv_WithoutHeader() throws Exception {
        // TSV without header - first row is treated as data
        String tsvContent = """
                BAR001\t100
                BAR002\t200
                """;
        
        MultipartFile file = new MockMultipartFile(
            "file", 
            "inventory.tsv", 
            "text/tab-separated-values", 
            tsvContent.getBytes()
        );

        // Test the static utility method directly
        List<InventoryUploadForm> result = com.increff.pos.util.TsvParseUtils.parseInventoryTsv(file);
        
        // First row is skipped as header, only second row processed
        assertEquals(1, result.size()); // Only 1 row processed (header skipped)
        
        InventoryUploadForm form = result.get(0);
        assertEquals("bar002", form.getBarcode());
        assertEquals(Integer.valueOf(200), form.getQuantity());
    }

    @Test
    void testProductTsv_WithHeader() throws Exception {
        // TSV with proper header
        String tsvContent = "productName\tmrp\tclientName\tbarcode\n" +
                "Test Product 1\t10.99\tClient 1\tBAR001\n" +
                "Test Product 2\t20.50\tClient 2\tBAR002\n";
        
        MultipartFile file = new MockMultipartFile(
            "file", 
            "products.tsv", 
            "text/tab-separated-values", 
            tsvContent.getBytes()
        );

        // Test the static utility method directly
        List<ProductUploadForm> result = com.increff.pos.util.TsvParseUtils.parseProductTsv(file);
        
        assertEquals(2, result.size()); // Both data rows processed
        
        ProductUploadForm form1 = result.get(0);
        assertEquals("test product 1", form1.getProductName());
        assertEquals("10.99", form1.getMrp().toString());
        assertEquals("client 1", form1.getClientName());
        assertEquals("bar001", form1.getBarcode());
        
        ProductUploadForm form2 = result.get(1);
        assertEquals("test product 2", form2.getProductName());
        assertEquals("20.50", form2.getMrp().toString());
        assertEquals("client 2", form2.getClientName());
        assertEquals("bar002", form2.getBarcode());
    }

    @Test
    void testInventoryTsv_WithHeader() throws Exception {
        // TSV with proper header
        String tsvContent = "barcode\tquantity\n" +
                "BAR001\t100\n" +
                "BAR002\t200\n";
        
        MultipartFile file = new MockMultipartFile(
            "file", 
            "inventory.tsv", 
            "text/tab-separated-values", 
            tsvContent.getBytes()
        );

        // Test the static utility method directly
        List<InventoryUploadForm> result = com.increff.pos.util.TsvParseUtils.parseInventoryTsv(file);
        
        assertEquals(2, result.size()); // Both data rows processed
        
        InventoryUploadForm form1 = result.get(0);
        assertEquals("bar001", form1.getBarcode());
        assertEquals(Integer.valueOf(100), form1.getQuantity());
        
        InventoryUploadForm form2 = result.get(1);
        assertEquals("bar002", form2.getBarcode());
        assertEquals(Integer.valueOf(200), form2.getQuantity());
    }

    @Test
    void testEmptyTsv_WithHeaderOnly() throws Exception {
        // TSV with only header
        String tsvContent = "barcode\tquantity\n";
        
        MultipartFile file = new MockMultipartFile(
            "file", 
            "inventory.tsv", 
            "text/tab-separated-values", 
            tsvContent.getBytes()
        );

        // Test the static utility method directly
        List<InventoryUploadForm> result = com.increff.pos.util.TsvParseUtils.parseInventoryTsv(file);
        
        assertEquals(0, result.size()); // No data rows
    }

}
