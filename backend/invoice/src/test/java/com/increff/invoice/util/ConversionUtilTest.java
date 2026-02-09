package com.increff.invoice.util;

import com.increff.invoice.model.form.InvoiceForm;
import com.increff.invoice.model.form.InvoiceItemForm;
import com.increff.invoice.model.internal.InvoiceItemModel;
import com.increff.invoice.model.internal.InvoiceModel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ConversionUtilTest {

    @Test
    public void testConvertInvoiceFormToModel_WithCompleteForm() {
        InvoiceItemForm itemForm = new InvoiceItemForm();
        itemForm.setProductName("Test Product");
        itemForm.setQuantity(2);
        itemForm.setSellingPrice(new BigDecimal("50.00"));
        itemForm.setLineTotal(new BigDecimal("100.00"));

        InvoiceForm form = new InvoiceForm();
        form.setInvoiceNumber("INV-001");
        form.setInvoiceDate(ZonedDateTime.now());
        form.setClientName("Test Client");
        form.setTotalAmount(new BigDecimal("100.00"));
        form.setItems(Collections.singletonList(itemForm));

        InvoiceModel result = ConversionUtil.convertInvoiceFormToModel(form);

        assertEquals("INV-001", result.getInvoiceNumber());
        assertEquals(form.getInvoiceDate(), result.getInvoiceDate());
        assertEquals("Test Client", result.getClientName());
        assertEquals(new BigDecimal("100.00"), result.getTotalAmount());
        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());
        
        InvoiceItemModel resultItem = result.getItems().get(0);
        assertEquals("Test Product", resultItem.getProductName());
        assertEquals(2, resultItem.getQuantity());
        assertEquals(new BigDecimal("50.00"), resultItem.getSellingPrice());
        assertEquals(new BigDecimal("100.00"), resultItem.getLineTotal());
    }

    @Test
    public void testConvertInvoiceFormToModel_WithEmptyItems() {
        InvoiceForm form = new InvoiceForm();
        form.setInvoiceNumber("INV-002");
        form.setInvoiceDate(ZonedDateTime.now());
        form.setClientName("Test Client");
        form.setTotalAmount(BigDecimal.ZERO);
        form.setItems(Collections.emptyList());

        InvoiceModel result = ConversionUtil.convertInvoiceFormToModel(form);

        assertEquals("INV-002", result.getInvoiceNumber());
        assertEquals(form.getInvoiceDate(), result.getInvoiceDate());
        assertEquals("Test Client", result.getClientName());
        assertEquals(BigDecimal.ZERO, result.getTotalAmount());
        assertNotNull(result.getItems());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    public void testConvertInvoiceFormToModel_WithNullItems() {
        InvoiceForm form = new InvoiceForm();
        form.setInvoiceNumber("INV-003");
        form.setInvoiceDate(ZonedDateTime.now());
        form.setClientName("Test Client");
        form.setTotalAmount(new BigDecimal("200.00"));
        form.setItems(null);

        InvoiceModel result = ConversionUtil.convertInvoiceFormToModel(form);

        assertEquals("INV-003", result.getInvoiceNumber());
        assertEquals(form.getInvoiceDate(), result.getInvoiceDate());
        assertEquals("Test Client", result.getClientName());
        assertEquals(new BigDecimal("200.00"), result.getTotalAmount());
        assertNotNull(result.getItems());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    public void testConvertInvoiceFormToModel_WithMultipleItems() {
        InvoiceItemForm item1 = new InvoiceItemForm();
        item1.setProductName("Product 1");
        item1.setQuantity(1);
        item1.setSellingPrice(new BigDecimal("10.00"));
        item1.setLineTotal(new BigDecimal("10.00"));

        InvoiceItemForm item2 = new InvoiceItemForm();
        item2.setProductName("Product 2");
        item2.setQuantity(3);
        item2.setSellingPrice(new BigDecimal("20.00"));
        item2.setLineTotal(new BigDecimal("60.00"));

        InvoiceForm form = new InvoiceForm();
        form.setInvoiceNumber("INV-004");
        form.setInvoiceDate(ZonedDateTime.now());
        form.setClientName("Test Client");
        form.setTotalAmount(new BigDecimal("70.00"));
        form.setItems(List.of(item1, item2));

        InvoiceModel result = ConversionUtil.convertInvoiceFormToModel(form);

        assertEquals("INV-004", result.getInvoiceNumber());
        assertEquals(new BigDecimal("70.00"), result.getTotalAmount());
        assertNotNull(result.getItems());
        assertEquals(2, result.getItems().size());

        InvoiceItemModel resultItem1 = result.getItems().get(0);
        assertEquals("Product 1", resultItem1.getProductName());
        assertEquals(1, resultItem1.getQuantity());
        assertEquals(new BigDecimal("10.00"), resultItem1.getSellingPrice());
        assertEquals(new BigDecimal("10.00"), resultItem1.getLineTotal());

        InvoiceItemModel resultItem2 = result.getItems().get(1);
        assertEquals("Product 2", resultItem2.getProductName());
        assertEquals(3, resultItem2.getQuantity());
        assertEquals(new BigDecimal("20.00"), resultItem2.getSellingPrice());
        assertEquals(new BigDecimal("60.00"), resultItem2.getLineTotal());
    }

    @Test
    public void testConvertInvoiceItemToModel_WithCompleteItem() {
        InvoiceItemForm form = new InvoiceItemForm();
        form.setProductName("Test Product");
        form.setQuantity(5);
        form.setSellingPrice(new BigDecimal("25.50"));
        form.setLineTotal(new BigDecimal("127.50"));

        InvoiceItemModel result = ConversionUtil.convertInvoiceItemToModel(form);

        assertEquals("Test Product", result.getProductName());
        assertEquals(5, result.getQuantity());
        assertEquals(new BigDecimal("25.50"), result.getSellingPrice());
        assertEquals(new BigDecimal("127.50"), result.getLineTotal());
    }

    @Test
    public void testConvertInvoiceItemToModel_WithNullValues() {
        InvoiceItemForm form = new InvoiceItemForm();
        form.setProductName(null);
        form.setQuantity(0);
        form.setSellingPrice(null);
        form.setLineTotal(null);

        InvoiceItemModel result = ConversionUtil.convertInvoiceItemToModel(form);

        assertNull(result.getProductName());
        assertEquals(0, result.getQuantity());
        assertNull(result.getSellingPrice());
        assertNull(result.getLineTotal());
    }

    @Test
    public void testConvertInvoiceItemToModel_WithZeroValues() {
        InvoiceItemForm form = new InvoiceItemForm();
        form.setProductName("Zero Product");
        form.setQuantity(0);
        form.setSellingPrice(BigDecimal.ZERO);
        form.setLineTotal(BigDecimal.ZERO);

        InvoiceItemModel result = ConversionUtil.convertInvoiceItemToModel(form);

        assertEquals("Zero Product", result.getProductName());
        assertEquals(0, result.getQuantity());
        assertEquals(BigDecimal.ZERO, result.getSellingPrice());
        assertEquals(BigDecimal.ZERO, result.getLineTotal());
    }

    @Test
    public void testConvertInvoiceItemToModel_WithNegativeValues() {
        InvoiceItemForm form = new InvoiceItemForm();
        form.setProductName("Negative Product");
        form.setQuantity(-1);
        form.setSellingPrice(new BigDecimal("-10.00"));
        form.setLineTotal(new BigDecimal("-10.00"));

        InvoiceItemModel result = ConversionUtil.convertInvoiceItemToModel(form);

        assertEquals("Negative Product", result.getProductName());
        assertEquals(-1, result.getQuantity());
        assertEquals(new BigDecimal("-10.00"), result.getSellingPrice());
        assertEquals(new BigDecimal("-10.00"), result.getLineTotal());
    }

    @Test
    public void testConvertInvoiceFormToModel_WithNullForm() {
        assertThrows(NullPointerException.class, 
            () -> ConversionUtil.convertInvoiceFormToModel(null));
    }

    @Test
    public void testConvertInvoiceItemToModel_WithNullForm() {
        assertThrows(NullPointerException.class, 
            () -> ConversionUtil.convertInvoiceItemToModel(null));
    }

    @Test
    public void testConvertInvoiceFormToModel_WithLargeValues() {
        InvoiceItemForm itemForm = new InvoiceItemForm();
        itemForm.setProductName("Expensive Product");
        itemForm.setQuantity(Integer.MAX_VALUE);
        itemForm.setSellingPrice(new BigDecimal("999999999.99"));
        itemForm.setLineTotal(new BigDecimal("999999999.99"));

        InvoiceForm form = new InvoiceForm();
        form.setInvoiceNumber("INV-LARGE");
        form.setInvoiceDate(ZonedDateTime.now());
        form.setClientName("Wealthy Client");
        form.setTotalAmount(new BigDecimal("999999999.99"));
        form.setItems(Collections.singletonList(itemForm));

        InvoiceModel result = ConversionUtil.convertInvoiceFormToModel(form);

        assertEquals("INV-LARGE", result.getInvoiceNumber());
        assertEquals(Integer.MAX_VALUE, result.getItems().get(0).getQuantity());
        assertEquals(new BigDecimal("999999999.99"), result.getItems().get(0).getSellingPrice());
        assertEquals(new BigDecimal("999999999.99"), result.getTotalAmount());
    }
}
