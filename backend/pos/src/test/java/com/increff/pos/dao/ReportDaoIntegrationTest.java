package com.increff.pos.dao;

import com.increff.pos.domain.OrderStatus;
import com.increff.pos.entity.ClientEntity;
import com.increff.pos.entity.OrderEntity;
import com.increff.pos.entity.OrderItemEntity;
import com.increff.pos.entity.ProductEntity;
import com.increff.pos.model.internal.DaySalesAggregate;
import com.increff.pos.model.internal.SalesReportRow;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ReportDaoIntegrationTest {

    @Autowired
    private ReportDao reportDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private ClientDao clientDao;

    private static int barcodeCounter = 1;

    private void createTestData() {
        ClientEntity client = new ClientEntity();
        client.setClientName("Test Client " + System.currentTimeMillis());
        client.setEnabled(true);
        client = clientDao.save(client);

        ProductEntity product = new ProductEntity();
        product.setProductName("Test Product");
        product.setMrp(new BigDecimal("100.00"));
        product.setClientId(client.getId());
        product.setBarcode("BARCODE" + (barcodeCounter++));
        product = productDao.save(product);

        OrderEntity order = new OrderEntity();
        order.setClientId(client.getId());
        order.setStatus(OrderStatus.INVOICED);
        order = orderDao.save(order);

        OrderItemEntity item = new OrderItemEntity();
        item.setOrderId(order.getId());
        item.setProductId(product.getId());
        item.setQuantity(5);
        item.setSellingPrice(new BigDecimal("80.00"));

        orderItemDao.saveAll(List.of(item));
    }

    @Test
    void testGetSalesReport() {
        createTestData();

        ZonedDateTime startDate = ZonedDateTime.now().minusDays(1);
        ZonedDateTime endDate = ZonedDateTime.now().plusDays(1);
        Pageable pageable = PageRequest.of(0, 10);

        Page<SalesReportRow> report = reportDao.getSalesReport(startDate, endDate, null, pageable);

        assertEquals(1, report.getTotalElements());
        assertEquals(1, report.getContent().size());

        SalesReportRow row = report.getContent().get(0);
        assertEquals("Test Product", row.getProductName());
        assertEquals(5, row.getQuantitySold());
        assertEquals(new BigDecimal("400.00"), row.getRevenue());
    }

    @Test
    void testGetSalesReportWithClientId() {
        createTestData();

        ZonedDateTime startDate = ZonedDateTime.now().minusDays(1);
        ZonedDateTime endDate = ZonedDateTime.now().plusDays(1);
        Pageable pageable = PageRequest.of(0, 10);

        List<ClientEntity> clients = clientDao.selectAll();
        Integer clientId = clients.get(0).getId();

        Page<SalesReportRow> report = reportDao.getSalesReport(startDate, endDate, clientId, pageable);

        assertEquals(1, report.getTotalElements());
        assertEquals(1, report.getContent().size());

        SalesReportRow row = report.getContent().get(0);
        assertEquals("Test Product", row.getProductName());
        assertEquals(5, row.getQuantitySold());
        assertEquals(new BigDecimal("400.00"), row.getRevenue());
    }

    @Test
    void testGetAllSalesReport() {
        createTestData();

        ZonedDateTime startDate = ZonedDateTime.now().minusDays(1);
        ZonedDateTime endDate = ZonedDateTime.now().plusDays(1);

        List<SalesReportRow> report = reportDao.getAllSalesReport(startDate, endDate, null);

        assertEquals(1, report.size());

        SalesReportRow row = report.get(0);
        assertEquals("Test Product", row.getProductName());
        assertEquals(5, row.getQuantitySold());
        assertEquals(new BigDecimal("400.00"), row.getRevenue());
    }

    @Test
    void testGetDaySalesAggregate() {
        createTestData();

        ZonedDateTime utcStart = ZonedDateTime.now().minusDays(1);
        ZonedDateTime utcEnd = ZonedDateTime.now().plusDays(1);

        DaySalesAggregate aggregate = reportDao.getDaySalesAggregate(utcStart, utcEnd);

        assertNotNull(aggregate);
        assertEquals(1L, aggregate.getInvoicedOrdersCount());
        assertEquals(5L, aggregate.getInvoicedItemsCount());
        assertEquals(new BigDecimal("400.00"), aggregate.getTotalRevenue());
    }
}
