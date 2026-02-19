package com.increff.pos.model;

import com.increff.pos.model.domain.OrderStatus;
import com.increff.pos.model.domain.UserRole;
import com.increff.pos.model.data.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataTests {

    @Test
    void testAuthData() {
        AuthData d = new AuthData();
        d.setUserId(1);
        d.setRole(UserRole.OPERATOR);
        assertEquals(1, d.getUserId());
        assertEquals(UserRole.OPERATOR, d.getRole());
    }

    @Test
    void testClientData() {
        ClientData d = new ClientData();
        d.setId(1);
        d.setClientName("Test Client");
        d.setEnabled(true);
        assertEquals(1, d.getId());
        assertEquals("Test Client", d.getClientName());
        assertEquals(true, d.getEnabled());
    }

    @Test
    void testDaySalesData() {
        DaySalesData d = new DaySalesData();
        d.setDate(LocalDate.of(2023, 1, 1));
        d.setInvoicedOrdersCount(10);
        d.setInvoicedItemsCount(50);
        d.setTotalRevenue(new BigDecimal("1000.00"));
        assertEquals(LocalDate.of(2023, 1, 1), d.getDate());
        assertEquals(10, d.getInvoicedOrdersCount());
        assertEquals(50, d.getInvoicedItemsCount());
        assertEquals(new BigDecimal("1000.00"), d.getTotalRevenue());
    }

    @Test
    void testDaySalesPageData() {
        DaySalesPageData d = new DaySalesPageData();
        List<DaySalesData> content = new ArrayList<>();
        DaySalesData daySales = new DaySalesData();
        daySales.setDate(LocalDate.of(2023, 1, 1));
        content.add(daySales);
        d.setContent(content);
        d.setPage(0);
        d.setPageSize(10);
        d.setTotalElements(100L);
        assertEquals(content, d.getContent());
        assertEquals(0, d.getPage());
        assertEquals(10, d.getPageSize());
        assertEquals(100L, d.getTotalElements());
    }

    @Test
    void testErrorData() {
        ErrorData d = new ErrorData("ERR001", "Test error");
        assertEquals("ERR001", d.getCode());
        assertEquals("Test error", d.getMessage());
        
        ErrorData d2 = new ErrorData("ERR002", "Another error", new ArrayList<>());
        assertEquals("ERR002", d2.getCode());
        assertEquals("Another error", d2.getMessage());
        
        d.addFieldError("field1", "Field error message");
        assertEquals(1, d.getFieldErrors().size());
        assertEquals("field1", d.getFieldErrors().get(0).getField());
    }

    @Test
    void testFieldErrorData() {
        FieldErrorData d = new FieldErrorData();
        d.setField("testField");
        d.setMessage("Test message");
        d.setCode("ERR001");
        assertEquals("testField", d.getField());
        assertEquals("Test message", d.getMessage());
        assertEquals("ERR001", d.getCode());
        
        FieldErrorData d2 = new FieldErrorData("field2", "message2", "code2");
        assertEquals("field2", d2.getField());
        assertEquals("message2", d2.getMessage());
        assertEquals("code2", d2.getCode());
    }

    @Test
    void testInventoryData() {
        InventoryData d = new InventoryData();
        d.setProductId(1);
        d.setProductName("Test Product");
        d.setQuantity(100);
        assertEquals(1, d.getProductId());
        assertEquals("Test Product", d.getProductName());
        assertEquals(100, d.getQuantity());
    }

    @Test
    void testInvoiceData() {
        InvoiceData d = new InvoiceData();
        d.setBase64Pdf("base64encodedpdf");
        assertEquals("base64encodedpdf", d.getBase64Pdf());
    }

    @Test
    void testInvoiceSummaryData() {
        InvoiceSummaryData d = new InvoiceSummaryData();
        d.setOrderId(1);
        d.setCreatedAt(ZonedDateTime.now());
        assertEquals(1, d.getOrderId());
    }

    @Test
    void testOrderData() {
        OrderData d = new OrderData();
        d.setOrderId(1);
        d.setClientId(2);
        d.setCreatedAt(ZonedDateTime.now());
        d.setStatus(OrderStatus.INVOICED);
        
        List<OrderItemData> items = new ArrayList<>();
        OrderItemData item = new OrderItemData();
        item.setProductId(1);
        items.add(item);
        d.setItems(items);
        
        assertEquals(1, d.getOrderId());
        assertEquals(2, d.getClientId());
        assertEquals(OrderStatus.INVOICED, d.getStatus());
        assertEquals(items, d.getItems());
        assertEquals(1, d.getItems().get(0).getProductId());
    }

    @Test
    void testOrderItemData() {
        OrderItemData d = new OrderItemData();
        d.setProductId(1);
        d.setProductName("Test Product");
        d.setQuantity(5);
        d.setSellingPrice(new BigDecimal("75.50"));
        assertEquals(1, d.getProductId());
        assertEquals("Test Product", d.getProductName());
        assertEquals(5, d.getQuantity());
        assertEquals(new BigDecimal("75.50"), d.getSellingPrice());
    }

    @Test
    void testOrderPageData() {
        OrderPageData d = new OrderPageData();
        List<OrderData> content = new ArrayList<>();
        OrderData order = new OrderData();
        order.setOrderId(1);
        content.add(order);
        d.setContent(content);
        d.setPage(0);
        d.setPageSize(20);
        d.setTotalElements(200L);
        assertEquals(content, d.getContent());
        assertEquals(0, d.getPage());
        assertEquals(20, d.getPageSize());
        assertEquals(200L, d.getTotalElements());
    }

    @Test
    void testProductData() {
        ProductData d = new ProductData();
        d.setId(1);
        d.setProductName("Test Product");
        d.setMrp(new BigDecimal("100.00"));
        d.setClientId(2);
        d.setClientName("Test Client");
        d.setBarcode("123456789");
        d.setImageUrl("http://example.com/image.jpg");
        assertEquals(1, d.getId());
        assertEquals("Test Product", d.getProductName());
        assertEquals(new BigDecimal("100.00"), d.getMrp());
        assertEquals(2, d.getClientId());
        assertEquals("Test Client", d.getClientName());
        assertEquals("123456789", d.getBarcode());
        assertEquals("http://example.com/image.jpg", d.getImageUrl());
    }

    @Test
    void testSalesReportPageData() {
        SalesReportPageData d = new SalesReportPageData();
        List<SalesReportRowData> rows = new ArrayList<>();
        SalesReportRowData row = new SalesReportRowData();
        row.setProductName("Test Product");
        rows.add(row);
        d.setRows(rows);
        d.setPage(0);
        d.setPageSize(10);
        d.setTotalElements(50L);
        assertEquals(rows, d.getRows());
        assertEquals(0, d.getPage());
        assertEquals(10, d.getPageSize());
        assertEquals(50L, d.getTotalElements());
    }

    @Test
    void testSalesReportRowData() {
        SalesReportRowData d = new SalesReportRowData();
        d.setProductName("Test Product");
        d.setQuantitySold(100);
        d.setRevenue(1500.75);
        assertEquals("Test Product", d.getProductName());
        assertEquals(100, d.getQuantitySold());
        assertEquals(1500.75, d.getRevenue());
    }

    @Test
    void testUserData() {
        UserData d = new UserData();
        d.setId(1);
        d.setEmail("test@example.com");
        d.setRole(UserRole.SUPERVISOR);
        assertEquals(1, d.getId());
        assertEquals("test@example.com", d.getEmail());
        assertEquals(UserRole.SUPERVISOR, d.getRole());
    }
}
