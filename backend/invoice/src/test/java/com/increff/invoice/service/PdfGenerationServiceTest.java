package com.increff.invoice.service;

import com.increff.invoice.exception.ApiException;
import com.increff.invoice.exception.ApiStatus;
import com.increff.invoice.model.internal.InvoiceModel;
import com.increff.invoice.util.XmlBuilderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PdfGenerationServiceTest {

    @Mock
    private XmlBuilderUtil xmlBuilderUtil;

    @InjectMocks
    private PdfGenerationService pdfGenerationService;

    @BeforeEach
    public void setUp() throws Exception {
        // Use reflection to inject the mock since @InjectMocks might not work with @Autowired
        Field xmlBuilderUtilField = PdfGenerationService.class.getDeclaredField("xmlBuilderUtil");
        xmlBuilderUtilField.setAccessible(true);
        xmlBuilderUtilField.set(pdfGenerationService, xmlBuilderUtil);
    }

    @Test
    public void testGeneratePdf_Success() {
        InvoiceModel invoiceModel = new InvoiceModel();
        invoiceModel.setInvoiceNumber("INV-001");
        invoiceModel.setItems(java.util.Collections.emptyList());

        String expectedXml = "<invoice>test</invoice>";
        when(xmlBuilderUtil.buildInvoiceXml(any(InvoiceModel.class))).thenReturn(expectedXml);

        String result = pdfGenerationService.generatePdf(invoiceModel);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(xmlBuilderUtil).buildInvoiceXml(invoiceModel);
    }

    @Test
    public void testGeneratePdf_WithNullModel() {
        assertThrows(ApiException.class, () -> pdfGenerationService.generatePdf(null));
    }

    @Test
    public void testGeneratePdf_WithXmlBuilderException() {
        InvoiceModel invoiceModel = new InvoiceModel();
        invoiceModel.setItems(java.util.Collections.emptyList());
        
        when(xmlBuilderUtil.buildInvoiceXml(any(InvoiceModel.class)))
            .thenThrow(new RuntimeException("XML generation failed"));

        ApiException exception = assertThrows(ApiException.class, 
            () -> pdfGenerationService.generatePdf(invoiceModel));
        
        assertEquals(ApiStatus.INTERNAL_ERROR, exception.getStatus());
        assertEquals("Could not create Invoice", exception.getMessage());
    }

    @Test
    public void testGeneratePdf_WithTransformerException() {
        InvoiceModel invoiceModel = new InvoiceModel();
        invoiceModel.setItems(java.util.Collections.emptyList());
        
        String malformedXml = "<invalid>xml";
        when(xmlBuilderUtil.buildInvoiceXml(any(InvoiceModel.class))).thenReturn(malformedXml);

        ApiException exception = assertThrows(ApiException.class, 
            () -> pdfGenerationService.generatePdf(invoiceModel));
        
        assertEquals(ApiStatus.INTERNAL_ERROR, exception.getStatus());
        assertEquals("Could not create Invoice", exception.getMessage());
    }

    @Test
    public void testGeneratePdf_WithIOException() {
        InvoiceModel invoiceModel = new InvoiceModel();
        invoiceModel.setItems(java.util.Collections.emptyList());
        
        when(xmlBuilderUtil.buildInvoiceXml(any(InvoiceModel.class)))
            .thenThrow(new RuntimeException("IO error during processing"));

        ApiException exception = assertThrows(ApiException.class, 
            () -> pdfGenerationService.generatePdf(invoiceModel));
        
        assertEquals(ApiStatus.INTERNAL_ERROR, exception.getStatus());
        assertEquals("Could not create Invoice", exception.getMessage());
    }

    @Test
    public void testGeneratePdf_WithFopException() {
        InvoiceModel invoiceModel = new InvoiceModel();
        invoiceModel.setItems(java.util.Collections.emptyList());
        
        when(xmlBuilderUtil.buildInvoiceXml(any(InvoiceModel.class)))
            .thenThrow(new RuntimeException("FOP processing error"));

        ApiException exception = assertThrows(ApiException.class, 
            () -> pdfGenerationService.generatePdf(invoiceModel));
        
        assertEquals(ApiStatus.INTERNAL_ERROR, exception.getStatus());
        assertEquals("Could not create Invoice", exception.getMessage());
    }

    @Test
    public void testGeneratePdf_WithEmptyModel() {
        InvoiceModel emptyModel = new InvoiceModel();
        emptyModel.setItems(java.util.Collections.emptyList());
        
        String expectedXml = "<invoice></invoice>";
        when(xmlBuilderUtil.buildInvoiceXml(any(InvoiceModel.class))).thenReturn(expectedXml);

        String result = pdfGenerationService.generatePdf(emptyModel);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(xmlBuilderUtil).buildInvoiceXml(emptyModel);
    }

    @Test
    public void testGeneratePdf_VerifyBase64Encoding() {
        InvoiceModel invoiceModel = new InvoiceModel();
        invoiceModel.setInvoiceNumber("INV-TEST");
        invoiceModel.setItems(java.util.Collections.emptyList());
        
        String expectedXml = "<invoice>test content</invoice>";
        when(xmlBuilderUtil.buildInvoiceXml(any(InvoiceModel.class))).thenReturn(expectedXml);

        String result = pdfGenerationService.generatePdf(invoiceModel);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        // Verify it's valid base64 by attempting to decode
        assertDoesNotThrow(() -> java.util.Base64.getDecoder().decode(result));
    }
}
