package com.increff.invoice.util;

import com.increff.invoice.model.internal.InvoiceItemModel;
import com.increff.invoice.model.internal.InvoiceModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class XmlBuilderUtilTest {

    @InjectMocks
    private XmlBuilderUtil xmlBuilderUtil;

    @Test
    public void testBuildInvoiceXml_WithCompleteInvoice() {
        InvoiceItemModel item = new InvoiceItemModel();
        item.setProductName("Test Product");
        item.setQuantity(2);
        item.setSellingPrice(new BigDecimal("50.00"));
        item.setLineTotal(new BigDecimal("100.00"));

        InvoiceModel invoice = new InvoiceModel();
        invoice.setInvoiceNumber("INV-001");
        invoice.setInvoiceDate(ZonedDateTime.parse("2023-01-15T10:30:00Z"));
        invoice.setClientName("Test Client");
        invoice.setItems(Collections.singletonList(item));
        invoice.setTotalAmount(new BigDecimal("100.00"));

        String result = xmlBuilderUtil.buildInvoiceXml(invoice);

        assertNotNull(result);
        assertTrue(result.contains("<invoice>"));
        assertTrue(result.contains("</invoice>"));
        assertTrue(result.contains("<invoiceNumber>INV-001</invoiceNumber>"));
        assertTrue(result.contains("<clientName>Test Client</clientName>"));
        assertTrue(result.contains("<items>"));
        assertTrue(result.contains("<item>"));
        assertTrue(result.contains("<productName>Test Product</productName>"));
        assertTrue(result.contains("<quantity>2</quantity>"));
        assertTrue(result.contains("<sellingPrice>50.00</sellingPrice>"));
        assertTrue(result.contains("<lineTotal>100.00</lineTotal>"));
        assertTrue(result.contains("</item>"));
        assertTrue(result.contains("</items>"));
        assertTrue(result.contains("<totalAmount>100.00</totalAmount>"));
        assertTrue(result.contains("<invoiceDate>"));
        assertTrue(result.contains("</invoiceDate>"));
    }

    @Test
    public void testBuildInvoiceXml_WithEmptyItems() {
        InvoiceModel invoice = new InvoiceModel();
        invoice.setInvoiceNumber("INV-002");
        invoice.setInvoiceDate(ZonedDateTime.parse("2023-01-15T10:30:00Z"));
        invoice.setClientName("Test Client");
        invoice.setItems(Collections.emptyList());
        invoice.setTotalAmount(BigDecimal.ZERO);

        String result = xmlBuilderUtil.buildInvoiceXml(invoice);

        assertNotNull(result);
        assertTrue(result.contains("<invoice>"));
        assertTrue(result.contains("<items>"));
        assertTrue(result.contains("</items>"));
        assertFalse(result.contains("<item>"));
        assertTrue(result.contains("<totalAmount>0</totalAmount>"));
    }

    @Test
    public void testBuildInvoiceXml_WithNullItems() {
        InvoiceModel invoice = new InvoiceModel();
        invoice.setInvoiceNumber("INV-003");
        invoice.setInvoiceDate(ZonedDateTime.parse("2023-01-15T10:30:00Z"));
        invoice.setClientName("Test Client");
        invoice.setItems(null);
        invoice.setTotalAmount(new BigDecimal("200.00"));

        assertThrows(NullPointerException.class, () -> xmlBuilderUtil.buildInvoiceXml(invoice));
    }

    @Test
    public void testBuildInvoiceXml_WithMultipleItems() {
        InvoiceItemModel item1 = new InvoiceItemModel();
        item1.setProductName("Product 1");
        item1.setQuantity(1);
        item1.setSellingPrice(new BigDecimal("10.00"));
        item1.setLineTotal(new BigDecimal("10.00"));

        InvoiceItemModel item2 = new InvoiceItemModel();
        item2.setProductName("Product 2");
        item2.setQuantity(3);
        item2.setSellingPrice(new BigDecimal("20.00"));
        item2.setLineTotal(new BigDecimal("60.00"));

        InvoiceModel invoice = new InvoiceModel();
        invoice.setInvoiceNumber("INV-004");
        invoice.setInvoiceDate(ZonedDateTime.parse("2023-01-15T10:30:00Z"));
        invoice.setClientName("Test Client");
        invoice.setItems(List.of(item1, item2));
        invoice.setTotalAmount(new BigDecimal("70.00"));

        String result = xmlBuilderUtil.buildInvoiceXml(invoice);

        assertNotNull(result);
        assertTrue(result.contains("<items>"));
        assertTrue(result.contains("<item>"));
        assertTrue(result.contains("<productName>Product 1</productName>"));
        assertTrue(result.contains("<productName>Product 2</productName>"));
        assertTrue(result.contains("<quantity>1</quantity>"));
        assertTrue(result.contains("<quantity>3</quantity>"));
        assertTrue(result.contains("</item>"));
        assertTrue(result.contains("</items>"));
    }

    @Test
    public void testBuildInvoiceXml_WithNullValues() {
        InvoiceItemModel item = new InvoiceItemModel();
        item.setProductName(null);
        item.setQuantity(0);
        item.setSellingPrice(null);
        item.setLineTotal(null);

        InvoiceModel invoice = new InvoiceModel();
        invoice.setInvoiceNumber(null);
        invoice.setInvoiceDate(null);
        invoice.setClientName(null);
        invoice.setItems(Collections.singletonList(item));
        invoice.setTotalAmount(null);

        String result = xmlBuilderUtil.buildInvoiceXml(invoice);

        assertNotNull(result);
        assertTrue(result.contains("<invoiceNumber>null</invoiceNumber>"));
        assertTrue(result.contains("<clientName>null</clientName>"));
        assertTrue(result.contains("<productName>null</productName>"));
        assertTrue(result.contains("<quantity>0</quantity>"));
        assertTrue(result.contains("<sellingPrice>null</sellingPrice>"));
        assertTrue(result.contains("<lineTotal>null</lineTotal>"));
        assertTrue(result.contains("<totalAmount>null</totalAmount>"));
    }

    @Test
    public void testBuildInvoiceXml_WithSpecialCharacters() {
        InvoiceItemModel item = new InvoiceItemModel();
        item.setProductName("Product & <Test> \"Quote\"");
        item.setQuantity(1);
        item.setSellingPrice(new BigDecimal("10.00"));
        item.setLineTotal(new BigDecimal("10.00"));

        InvoiceModel invoice = new InvoiceModel();
        invoice.setInvoiceNumber("INV-005");
        invoice.setInvoiceDate(ZonedDateTime.parse("2023-01-15T10:30:00Z"));
        invoice.setClientName("Client & Company <Ltd>");
        invoice.setItems(Collections.singletonList(item));
        invoice.setTotalAmount(new BigDecimal("10.00"));

        String result = xmlBuilderUtil.buildInvoiceXml(invoice);

        assertNotNull(result);
        assertTrue(result.contains("Product & <Test> \"Quote\""));
        assertTrue(result.contains("Client & Company <Ltd>"));
    }

    @Test
    public void testBuildInvoiceXml_DateFormatting() {
        InvoiceModel invoice = new InvoiceModel();
        invoice.setInvoiceNumber("INV-006");
        invoice.setInvoiceDate(ZonedDateTime.parse("2023-01-15T10:30:00Z"));
        invoice.setClientName("Test Client");
        invoice.setItems(Collections.emptyList());
        invoice.setTotalAmount(BigDecimal.ZERO);

        String result = xmlBuilderUtil.buildInvoiceXml(invoice);

        assertNotNull(result);
        assertTrue(result.contains("<invoiceDate>"));
        assertTrue(result.contains("</invoiceDate>"));
        // The date should be formatted in Asia/Kolkata timezone
        assertTrue(result.matches(".*<invoiceDate>\\d{2} \\w{3} \\d{4}, \\d{2}:\\d{2} [AP]M</invoiceDate>.*"));
    }

    @Test
    public void testBuildInvoiceXml_WithLargeValues() {
        InvoiceItemModel item = new InvoiceItemModel();
        item.setProductName("Expensive Product");
        item.setQuantity(Integer.MAX_VALUE);
        item.setSellingPrice(new BigDecimal("999999999.99"));
        item.setLineTotal(new BigDecimal("999999999.99"));

        InvoiceModel invoice = new InvoiceModel();
        invoice.setInvoiceNumber("INV-LARGE");
        invoice.setInvoiceDate(ZonedDateTime.parse("2023-01-15T10:30:00Z"));
        invoice.setClientName("Wealthy Client");
        invoice.setItems(Collections.singletonList(item));
        invoice.setTotalAmount(new BigDecimal("999999999.99"));

        String result = xmlBuilderUtil.buildInvoiceXml(invoice);

        assertNotNull(result);
        assertTrue(result.contains("<quantity>" + Integer.MAX_VALUE + "</quantity>"));
        assertTrue(result.contains("<sellingPrice>999999999.99</sellingPrice>"));
        assertTrue(result.contains("<lineTotal>999999999.99</lineTotal>"));
        assertTrue(result.contains("<totalAmount>999999999.99</totalAmount>"));
    }

    @Test
    public void testBuildInvoiceXml_WithNegativeValues() {
        InvoiceItemModel item = new InvoiceItemModel();
        item.setProductName("Discount Item");
        item.setQuantity(-1);
        item.setSellingPrice(new BigDecimal("-10.00"));
        item.setLineTotal(new BigDecimal("-10.00"));

        InvoiceModel invoice = new InvoiceModel();
        invoice.setInvoiceNumber("INV-NEG");
        invoice.setInvoiceDate(ZonedDateTime.parse("2023-01-15T10:30:00Z"));
        invoice.setClientName("Test Client");
        invoice.setItems(Collections.singletonList(item));
        invoice.setTotalAmount(new BigDecimal("-10.00"));

        String result = xmlBuilderUtil.buildInvoiceXml(invoice);

        assertNotNull(result);
        assertTrue(result.contains("<quantity>-1</quantity>"));
        assertTrue(result.contains("<sellingPrice>-10.00</sellingPrice>"));
        assertTrue(result.contains("<lineTotal>-10.00</lineTotal>"));
        assertTrue(result.contains("<totalAmount>-10.00</totalAmount>"));
    }

    @Test
    public void testBuildInvoiceXml_WithNullModel() {
        assertThrows(NullPointerException.class, () -> xmlBuilderUtil.buildInvoiceXml(null));
    }

    @Test
    public void testBuildInvoiceXml_XmlStructure() {
        InvoiceModel invoice = new InvoiceModel();
        invoice.setInvoiceNumber("INV-STRUCTURE");
        invoice.setInvoiceDate(ZonedDateTime.parse("2023-01-15T10:30:00Z"));
        invoice.setClientName("Test Client");
        invoice.setItems(Collections.emptyList());
        invoice.setTotalAmount(BigDecimal.ZERO);

        String result = xmlBuilderUtil.buildInvoiceXml(invoice);

        // Verify XML structure
        assertTrue(result.startsWith("<invoice>"));
        assertTrue(result.endsWith("</invoice>"));
        
        // Verify proper nesting
        int invoiceOpen = result.indexOf("<invoice>");
        int invoiceClose = result.lastIndexOf("</invoice>");
        assertTrue(invoiceOpen < invoiceClose);
        
        // Verify items section
        int itemsOpen = result.indexOf("<items>");
        int itemsClose = result.indexOf("</items>");
        assertTrue(itemsOpen < itemsClose);
        assertTrue(invoiceOpen < itemsOpen);
        assertTrue(itemsClose < invoiceClose);
    }
}
