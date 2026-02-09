package com.increff.pos.dao;

import com.increff.pos.entity.DaySalesEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class DaySalesDaoIntegrationTest {

    @Autowired
    private DaySalesDao daySalesDao;

    @Test
    void testInsert() {
        LocalDate date = LocalDate.now();
        
        DaySalesEntity daySales = new DaySalesEntity();
        daySales.setDate(date);
        daySales.setInvoicedOrdersCount(10);
        daySales.setInvoicedItemsCount(25);
        daySales.setTotalRevenue(new BigDecimal("1500.50"));

        DaySalesEntity saved = daySalesDao.save(daySales);

        assertNotNull(saved.getId());
        assertEquals(date, saved.getDate());
        assertEquals(10, saved.getInvoicedOrdersCount());
        assertEquals(25, saved.getInvoicedItemsCount());
        assertEquals(new BigDecimal("1500.50"), saved.getTotalRevenue());
    }

    @Test
    void testSelectByDate() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        
        DaySalesEntity daySales = new DaySalesEntity();
        daySales.setDate(date);
        daySales.setInvoicedOrdersCount(5);
        daySales.setInvoicedItemsCount(12);
        daySales.setTotalRevenue(new BigDecimal("750.25"));
        daySalesDao.save(daySales);

        Optional<DaySalesEntity> found = daySalesDao.findByDate(date);

        assertTrue(found.isPresent());
        assertEquals(date, found.get().getDate());
        assertEquals(5, found.get().getInvoicedOrdersCount());

        Optional<DaySalesEntity> notFound = daySalesDao.findByDate(LocalDate.of(2024, 1, 16));
        assertFalse(notFound.isPresent());
    }

    @Test
    void testSelectByDateRange() {
        LocalDate startDate = LocalDate.of(2024, 1, 10);
        LocalDate endDate = LocalDate.of(2024, 1, 15);
        
        DaySalesEntity daySales1 = new DaySalesEntity();
        daySales1.setDate(LocalDate.of(2024, 1, 10));
        daySales1.setInvoicedOrdersCount(5);
        daySales1.setInvoicedItemsCount(10);
        daySales1.setTotalRevenue(new BigDecimal("500.00"));
        daySalesDao.save(daySales1);

        DaySalesEntity daySales2 = new DaySalesEntity();
        daySales2.setDate(LocalDate.of(2024, 1, 12));
        daySales2.setInvoicedOrdersCount(8);
        daySales2.setInvoicedItemsCount(15);
        daySales2.setTotalRevenue(new BigDecimal("800.00"));
        daySalesDao.save(daySales2);

        DaySalesEntity daySales3 = new DaySalesEntity();
        daySales3.setDate(LocalDate.of(2024, 1, 20));
        daySales3.setInvoicedOrdersCount(3);
        daySales3.setInvoicedItemsCount(6);
        daySales3.setTotalRevenue(new BigDecimal("300.00"));
        daySalesDao.save(daySales3);

        List<DaySalesEntity> found = daySalesDao.findByDateRange(startDate, endDate);

        assertEquals(2, found.size());
        assertTrue(found.get(0).getDate().isAfter(found.get(1).getDate()) || 
                  found.get(0).getDate().equals(found.get(1).getDate()));
    }

    @Test
    void testDeleteByDate() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        
        DaySalesEntity daySales = new DaySalesEntity();
        daySales.setDate(date);
        daySales.setInvoicedOrdersCount(5);
        daySales.setInvoicedItemsCount(12);
        daySales.setTotalRevenue(new BigDecimal("750.25"));
        daySalesDao.save(daySales);

        Optional<DaySalesEntity> beforeDelete = daySalesDao.findByDate(date);
        assertTrue(beforeDelete.isPresent());

        daySalesDao.deleteByDate(date);

        Optional<DaySalesEntity> afterDelete = daySalesDao.findByDate(date);
        assertFalse(afterDelete.isPresent());
    }
}
