package com.increff.pos.controller;

import com.increff.pos.dto.InventoryDto;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.data.TsvUploadError;
import com.increff.pos.model.data.TsvUploadResult;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InventoryControllerExceptionTest {

    @Test
    void testUploadInventoryTsv_HandlesApiException() throws IOException {
        InventoryDto mockInventoryDto = mock(InventoryDto.class);
        InventoryController controller = new InventoryController();
        
        // Use reflection to set the private field
        try {
            java.lang.reflect.Field field = InventoryController.class.getDeclaredField("inventoryDto");
            field.setAccessible(true);
            field.set(controller, mockInventoryDto);
        } catch (Exception e) {
            fail("Failed to inject mock: " + e.getMessage());
        }

        // Mock ApiException from bulkUpsert (product not found)
        ApiException apiException = new ApiException(
            com.increff.pos.exception.ApiStatus.NOT_FOUND,
            "Product not found: 52",
            "productId",
            "Product not found: 52"
        );
        
        when(mockInventoryDto.uploadTsv(any())).thenThrow(apiException);

        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "inventory.tsv", 
            "text/tab-separated-values", 
            "productId\tquantity\n52\t100".getBytes()
        );

        ResponseEntity<?> response = controller.uploadInventoryTsv(file);

        // Should return error file instead of JSON error
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getHeaders().get("Content-Type").contains("application/octet-stream"));
        assertTrue(response.getHeaders().get("Content-Disposition").get(0).contains("attachment"));
        
        byte[] responseBytes = (byte[]) response.getBody();
        assertNotNull(responseBytes);
        
        String errorContent = new String(responseBytes);
        assertTrue(errorContent.contains("Product not found: 52"));
        assertTrue(errorContent.contains("Row Number\tOriginal Data\tError Message"));
    }

    @Test
    void testUploadInventoryTsv_HandlesValidationErrors() throws IOException {
        InventoryDto mockInventoryDto = mock(InventoryDto.class);
        InventoryController controller = new InventoryController();
        
        // Use reflection to set the private field
        try {
            java.lang.reflect.Field field = InventoryController.class.getDeclaredField("inventoryDto");
            field.setAccessible(true);
            field.set(controller, mockInventoryDto);
        } catch (Exception e) {
            fail("Failed to inject mock: " + e.getMessage());
        }

        // Mock validation errors from parsing
        List<TsvUploadError> validationErrors = List.of(
            new TsvUploadError(2, new String[]{"invalid", "100"}, "Invalid product ID format: invalid")
        );
        
        TsvUploadResult<InventoryData> errorResult = TsvUploadResult.failure(validationErrors);
        when(mockInventoryDto.uploadTsv(any())).thenReturn(errorResult);

        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "inventory.tsv", 
            "text/tab-separated-values", 
            "productId\tquantity\ninvalid\t100".getBytes()
        );

        ResponseEntity<?> response = controller.uploadInventoryTsv(file);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getHeaders().get("Content-Type").contains("application/octet-stream"));
        
        byte[] responseBytes = (byte[]) response.getBody();
        String errorContent = new String(responseBytes);
        assertTrue(errorContent.contains("Invalid product ID format: invalid"));
        assertTrue(errorContent.contains("2\t")); // Row number should be included
    }
}
