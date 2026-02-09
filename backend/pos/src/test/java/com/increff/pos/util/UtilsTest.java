package com.increff.pos.util;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @Test
    void should_normalize_string_when_valid_input() {
        // Arrange
        String input = "  TEST STRING  ";
        
        // Act
        String result = Utils.normalize(input);
        
        // Assert
        assertEquals("test string", result);
    }

    @Test
    void should_normalize_string_when_empty_input() {
        // Arrange
        String input = "  ";
        
        // Act
        String result = Utils.normalize(input);
        
        // Assert
        assertEquals("", result);
    }

    @Test
    void should_normalize_string_when_single_word() {
        // Arrange
        String input = "HELLO";
        
        // Act
        String result = Utils.normalize(input);
        
        // Assert
        assertEquals("hello", result);
    }

    @Test
    void should_parse_tsv_file_when_valid_content() {
        // Arrange
        String content = "Name\tAge\tCity\nJohn\t25\tNew York\nJane\t30\tLondon";
        MockMultipartFile file = new MockMultipartFile(
            "test.tsv", 
            "test.tsv", 
            "text/tab-separated-values", 
            content.getBytes()
        );
        
        // Act
        List<String[]> result = Utils.parse(file);
        
        // Assert
        assertEquals(2, result.size());
        assertEquals("John", result.getFirst()[0]);
        assertEquals("25", result.get(0)[1]);
        assertEquals("New York", result.get(0)[2]);
        assertEquals("Jane", result.get(1)[0]);
        assertEquals("30", result.get(1)[1]);
        assertEquals("London", result.get(1)[2]);
    }

    @Test
    void should_parse_tsv_file_when_empty_content() {
        // Arrange
        String content = "Name\tAge\tCity";
        MockMultipartFile file = new MockMultipartFile(
            "test.tsv", 
            "test.tsv", 
            "text/tab-separated-values", 
            content.getBytes()
        );
        
        // Act
        List<String[]> result = Utils.parse(file);
        
        // Assert
        assertEquals(0, result.size());
    }

    @Test
    void should_parse_tsv_file_when_mixed_columns() {
        // Arrange
        String content = "Name\tAge\tCity\nJohn\t25\nJane\t30\tLondon\tExtra";
        MockMultipartFile file = new MockMultipartFile(
            "test.tsv", 
            "test.tsv", 
            "text/tab-separated-values", 
            content.getBytes()
        );
        
        // Act
        List<String[]> result = Utils.parse(file);
        
        // Assert
        assertEquals(2, result.size());
        assertEquals(2, result.get(0).length); // John\t25 has 2 columns
        assertEquals("John", result.get(0)[0]);
        assertEquals("25", result.get(0)[1]);
        assertEquals(4, result.get(1).length); // Jane\t30\tLondon\tExtra has 4 columns
        assertEquals("Jane", result.get(1)[0]);
        assertEquals("30", result.get(1)[1]);
        assertEquals("London", result.get(1)[2]);
        assertEquals("Extra", result.get(1)[3]);
    }

    @Test
    void should_throw_exception_when_file_parsing_fails() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("test.txt", "test.txt", "text/plain", "content".getBytes()) {
            @Override
            public java.io.InputStream getInputStream() throws java.io.IOException {
                throw new java.io.IOException("Mock exception");
            }
        };

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> Utils.parse(file));
        assertEquals("Failed to parse TSV file", exception.getMessage());
    }
}
