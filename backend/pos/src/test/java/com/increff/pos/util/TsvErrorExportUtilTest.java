package com.increff.pos.util;

import com.increff.pos.model.data.TsvUploadError;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TsvErrorExportUtilTest {

    @Test
    void testExportErrorsToTsv() throws Exception {
        List<TsvUploadError> errors = List.of(
            new TsvUploadError(2, new String[]{"invalid", "100"}, "Invalid product ID format: invalid"),
            new TsvUploadError(3, new String[]{"1", "-50"}, "Quantity cannot be negative"),
            new TsvUploadError(4, new String[]{"1", "200"}, "Duplicate product ID: 1")
        );

        byte[] result = TsvErrorExportUtil.exportErrorsToTsv(errors, "inventory");

        assertNotNull(result);
        assertTrue(result.length > 0);

        String content = new String(result, StandardCharsets.UTF_8);
        
        // Check header
        assertTrue(content.contains("Row Number\tOriginal Data\tError Message"));
        
        // Check error rows
        assertTrue(content.contains("2\tinvalid | 100\tInvalid product ID format: invalid"));
        assertTrue(content.contains("3\t1 | -50\tQuantity cannot be negative"));
        assertTrue(content.contains("4\t1 | 200\tDuplicate product ID: 1"));
    }

    @Test
    void testGenerateErrorFilename() {
        String filename = TsvErrorExportUtil.generateErrorFilename("inventory");
        
        assertNotNull(filename);
        assertTrue(filename.startsWith("inventory-upload-errors-"));
        assertTrue(filename.endsWith(".tsv"));
        assertTrue(filename.contains("-"));
    }

    @Test
    void testExportErrorsToTsv_EmptyList() throws Exception {
        List<TsvUploadError> errors = List.of();

        byte[] result = TsvErrorExportUtil.exportErrorsToTsv(errors, "products");

        assertNotNull(result);
        
        String content = new String(result, StandardCharsets.UTF_8);
        
        // Should only contain header
        assertEquals("Row Number\tOriginal Data\tError Message\n", content);
    }

    @Test
    void testExportErrorsToTsv_WithSpecialCharacters() throws Exception {
        List<TsvUploadError> errors = List.of(
            new TsvUploadError(2, new String[]{"data\twith\ttabs", "100"}, "Error with\ttabs"),
            new TsvUploadError(3, new String[]{"data\nwith\nlines", "200"}, "Error with\nlines")
        );

        byte[] result = TsvErrorExportUtil.exportErrorsToTsv(errors, "test");

        String content = new String(result, StandardCharsets.UTF_8);
        
        // Check that content is generated and contains error information
        assertTrue(content.contains("Row Number\tOriginal Data\tError Message"));
        assertTrue(content.contains("2\t"));
        assertTrue(content.contains("3\t"));
        assertTrue(content.contains("Error with"));
        assertTrue(content.contains("data"));
        
        // Check that content is properly formatted (contains quotes for escaping)
        assertTrue(content.contains("\""));
    }
}
