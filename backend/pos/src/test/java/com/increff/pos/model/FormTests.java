package com.increff.pos.model;

import com.increff.pos.model.domain.OrderStatus;
import com.increff.pos.model.form.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FormTests {

    @Test
    void testClientForm() {
        ClientForm f = new ClientForm();
        f.setClientName("Test Client");
        assertEquals("Test Client", f.getClientName());
    }

    @Test
    void testClientSearchForm() {
        ClientSearchForm f = new ClientSearchForm();
        f.setClientName("Search Client");
        f.setEnabled(true);
        f.setPage(0);
        f.setPageSize(10);
        assertEquals("Search Client", f.getClientName());
        assertEquals(true, f.getEnabled());
        assertEquals(0, f.getPage());
        assertEquals(10, f.getPageSize());
    }

    @Test
    void testClientToggleForm() {
        ClientToggleForm f = new ClientToggleForm();
        f.setEnabled(false);
        assertEquals(false, f.getEnabled());
    }

    @Test
    void testDaySalesReportForm() {
        DaySalesReportForm f = new DaySalesReportForm();
        f.setStartDate(LocalDate.of(2023, 1, 1));
        f.setEndDate(LocalDate.of(2023, 1, 31));
        f.setPage(0);
        f.setPageSize(10);
        assertEquals(LocalDate.of(2023, 1, 1), f.getStartDate());
        assertEquals(LocalDate.of(2023, 1, 31), f.getEndDate());
        assertEquals(0, f.getPage());
        assertEquals(10, f.getPageSize());
    }

    @Test
    void testInventoryForm() {
        InventoryForm f = new InventoryForm();
        f.setProductId(1);
        f.setQuantity(100);
        assertEquals(1, f.getProductId());
        assertEquals(100, f.getQuantity());
    }

    @Test
    void testInventorySearchForm() {
        InventorySearchForm f = new InventorySearchForm();
        f.setPage(0);
        f.setPageSize(10);
        assertEquals(0, f.getPage());
        assertEquals(10, f.getPageSize());
    }

    @Test
    void testInvoiceForm() {
        InvoiceForm f = new InvoiceForm();
        f.setInvoiceNumber("INV-001");
        f.setInvoiceDate(ZonedDateTime.now());
        f.setClientName("Test Client");
        f.setTotalAmount(new BigDecimal("500.00"));
        
        List<InvoiceItemForm> items = new ArrayList<>();
        InvoiceItemForm item = new InvoiceItemForm();
        item.setProductName("Test Product");
        items.add(item);
        f.setItems(items);
        
        assertEquals("INV-001", f.getInvoiceNumber());
        assertEquals("Test Client", f.getClientName());
        assertEquals(new BigDecimal("500.00"), f.getTotalAmount());
        assertEquals(items, f.getItems());
        assertEquals("Test Product", f.getItems().get(0).getProductName());
    }

    @Test
    void testInvoiceItemForm() {
        InvoiceItemForm f = new InvoiceItemForm();
        f.setProductName("Test Product");
        f.setQuantity(5);
        f.setSellingPrice(new BigDecimal("99.99"));
        f.setLineTotal(new BigDecimal("499.95"));
        assertEquals("Test Product", f.getProductName());
        assertEquals(5, f.getQuantity());
        assertEquals(new BigDecimal("99.99"), f.getSellingPrice());
        assertEquals(new BigDecimal("499.95"), f.getLineTotal());
    }

    @Test
    void testOrderForm() {
        OrderForm f = new OrderForm();
        List<OrderItemForm> items = new ArrayList<>();
        OrderItemForm item = new OrderItemForm();
        item.setProductId(1);
        item.setQuantity(2);
        item.setSellingPrice(new BigDecimal("50.00"));
        items.add(item);
        f.setItems(items);
        assertEquals(items, f.getItems());
        assertEquals(1, f.getItems().get(0).getProductId());
    }

    @Test
    void testOrderItemForm() {
        OrderItemForm f = new OrderItemForm();
        f.setProductId(1);
        f.setQuantity(5);
        f.setSellingPrice(new BigDecimal("75.50"));
        assertEquals(1, f.getProductId());
        assertEquals(5, f.getQuantity());
        assertEquals(new BigDecimal("75.50"), f.getSellingPrice());
    }

    @Test
    void testOrderPageForm() {
        OrderPageForm f = new OrderPageForm();
        f.setStatus(OrderStatus.INVOICED);
        f.setClientId(1);
        f.setStartDate("2023-01-01");
        f.setEndDate("2023-01-31");
        f.setPage(0);
        f.setPageSize(20);
        assertEquals(OrderStatus.INVOICED, f.getStatus());
        assertEquals(1, f.getClientId());
        assertEquals("2023-01-01", f.getStartDate());
        assertEquals("2023-01-31", f.getEndDate());
        assertEquals(0, f.getPage());
        assertEquals(20, f.getPageSize());
    }

    @Test
    void testPageForm() {
        PageForm f = new PageForm();
        f.setPage(1);
        f.setPageSize(25);
        assertEquals(1, f.getPage());
        assertEquals(25, f.getPageSize());
    }

    @Test
    void testProductForm() {
        ProductForm f = new ProductForm();
        f.setProductName("Test Product");
        f.setMrp(new BigDecimal("100.00"));
        f.setClientId(1);
        f.setBarcode("123456789");
        f.setImageUrl("http://example.com/image.jpg");
        assertEquals("Test Product", f.getProductName());
        assertEquals(new BigDecimal("100.00"), f.getMrp());
        assertEquals(1, f.getClientId());
        assertEquals("123456789", f.getBarcode());
        assertEquals("http://example.com/image.jpg", f.getImageUrl());
    }

    @Test
    void testProductSearchForm() {
        ProductSearchForm f = new ProductSearchForm();
        f.setClientId(1);
        f.setBarcode("987654321");
        f.setProductName("Search Product");
        f.setPage(0);
        f.setPageSize(10);
        assertEquals(1, f.getClientId());
        assertEquals("987654321", f.getBarcode());
        assertEquals("Search Product", f.getProductName());
        assertEquals(0, f.getPage());
        assertEquals(10, f.getPageSize());
    }

    @Test
    void testSalesReportForm() {
        SalesReportForm f = new SalesReportForm();
        f.setStartDate(ZonedDateTime.now());
        f.setEndDate(ZonedDateTime.now().plusDays(30));
        f.setClientId(1);
        f.setPage(0);
        f.setPageSize(10);
        assertEquals(1, f.getClientId());
        assertEquals(0, f.getPage());
        assertEquals(10, f.getPageSize());
    }

    @Test
    void testUserForm() {
        UserForm f = new UserForm();
        f.setEmail("test@example.com");
        assertEquals("test@example.com", f.getEmail());
    }
}
