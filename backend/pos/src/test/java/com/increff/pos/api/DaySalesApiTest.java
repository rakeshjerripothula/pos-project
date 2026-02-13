package com.increff.pos.api;

import com.increff.pos.dao.DaySalesDao;
import com.increff.pos.dao.ReportDao;
import com.increff.pos.entity.DaySalesEntity;
import com.increff.pos.model.internal.DaySalesAggregate;
import com.increff.pos.util.ConversionUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DaySalesApiTest {

    @Mock
    private DaySalesDao daySalesDao;

    @Mock
    private ReportDao reportDao;

    @InjectMocks
    private DaySalesApi daySalesApi;

    @Test
    void should_calculate_day_sales_for_given_date() {
        // Arrange
        LocalDate date = LocalDate.of(2024, 1, 15);
        DaySalesAggregate aggregate = new DaySalesAggregate(19L, 50L, BigDecimal.valueOf(1000.0));

        DaySalesEntity expectedEntity = new DaySalesEntity();
        expectedEntity.setDate(date);
        expectedEntity.setTotalRevenue(BigDecimal.valueOf(1000.0));
        expectedEntity.setInvoicedOrdersCount(10);
        expectedEntity.setInvoicedItemsCount(50);

        when(reportDao.getDaySalesAggregate(any(), any())).thenReturn(aggregate);
        when(daySalesDao.save(any(DaySalesEntity.class))).thenReturn(expectedEntity);

        // Act
        daySalesApi.calculateForDate(date);

        // Assert
        verify(reportDao).getDaySalesAggregate(any(), any());
        verify(daySalesDao).save(any(DaySalesEntity.class));
    }

    @Test
    void should_find_day_sales_by_date_range_with_pagination() {
        // Arrange
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        Pageable pageable = mock(Pageable.class);

        DaySalesEntity entity1 = new DaySalesEntity();
        entity1.setDate(LocalDate.of(2024, 1, 15));
        entity1.setTotalRevenue(BigDecimal.valueOf(1000.0));

        DaySalesEntity entity2 = new DaySalesEntity();
        entity2.setDate(LocalDate.of(2024, 1, 16));
        entity2.setTotalRevenue(BigDecimal.valueOf(1500.0));

        Page<DaySalesEntity> expectedPage = new PageImpl<>(List.of(entity1, entity2));

        when(daySalesDao.findByDateRange(startDate, endDate, pageable)).thenReturn(expectedPage);

        // Act
        Page<DaySalesEntity> result = daySalesApi.findByDateRange(startDate, endDate, pageable);

        // Assert
        assertEquals(2, result.getContent().size());
        assertEquals(BigDecimal.valueOf(1000.0), result.getContent().get(0).getTotalRevenue());
        assertEquals(BigDecimal.valueOf(1500.0), result.getContent().get(1).getTotalRevenue());
        verify(daySalesDao).findByDateRange(startDate, endDate, pageable);
    }

    @Test
    void should_find_all_day_sales_by_date_range() {
        // Arrange
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        DaySalesEntity entity1 = new DaySalesEntity();
        entity1.setDate(LocalDate.of(2024, 1, 15));
        entity1.setTotalRevenue(BigDecimal.valueOf(1000.0));

        DaySalesEntity entity2 = new DaySalesEntity();
        entity2.setDate(LocalDate.of(2024, 1, 16));
        entity2.setTotalRevenue(BigDecimal.valueOf(1500.0));

        List<DaySalesEntity> expectedList = List.of(entity1, entity2);

        when(daySalesDao.findAllByDateRange(startDate, endDate)).thenReturn(expectedList);

        // Act
        List<DaySalesEntity> result = daySalesApi.findAllByDateRange(startDate, endDate);

        // Assert
        assertEquals(2, result.size());
        assertEquals(BigDecimal.valueOf(1000.0), result.get(0).getTotalRevenue());
        assertEquals(BigDecimal.valueOf(1500.0), result.get(1).getTotalRevenue());
        verify(daySalesDao).findAllByDateRange(startDate, endDate);
    }
}
