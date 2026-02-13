package com.increff.pos.api;

import com.increff.pos.dao.ReportDao;
import com.increff.pos.model.internal.SalesReportRow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportApiTest {

    @Mock
    private ReportDao reportDao;

    @InjectMocks
    private ReportApi reportApi;

    @Test
    void should_get_sales_report_with_pagination() {
        // Arrange
        ZonedDateTime startDate = ZonedDateTime.parse("2024-01-01T00:00:00Z");
        ZonedDateTime endDate = ZonedDateTime.parse("2024-01-31T23:59:59Z");
        Integer clientId = 1;
        org.springframework.data.domain.Pageable pageable = mock(org.springframework.data.domain.Pageable.class);

        SalesReportRow row1 = new SalesReportRow("Product A", 10, new BigDecimal("1000.00"));
        SalesReportRow row2 = new SalesReportRow("Product B", 5, new BigDecimal("500.00"));

        Page<SalesReportRow> expectedPage = new PageImpl<>(List.of(row1, row2));

        when(reportDao.getSalesReport(startDate, endDate, clientId, pageable)).thenReturn(expectedPage);

        // Act
        Page<SalesReportRow> result = reportApi.getSalesReport(startDate, endDate, clientId, pageable);

        // Assert
        assertEquals(2, result.getContent().size());
        assertEquals("Product A", result.getContent().get(0).getProductName());
        assertEquals(10, result.getContent().get(0).getQuantitySold());
        assertEquals(new BigDecimal("1000.00"), result.getContent().get(0).getRevenue());
        assertEquals("Product B", result.getContent().get(1).getProductName());
        assertEquals(5, result.getContent().get(1).getQuantitySold());
        assertEquals(new BigDecimal("500.00"), result.getContent().get(1).getRevenue());
        verify(reportDao).getSalesReport(startDate, endDate, clientId, pageable);
    }

    @Test
    void should_get_all_sales_report_without_pagination() {
        // Arrange
        ZonedDateTime startDate = ZonedDateTime.parse("2024-01-01T00:00:00Z");
        ZonedDateTime endDate = ZonedDateTime.parse("2024-01-31T23:59:59Z");
        Integer clientId = 2;

        SalesReportRow row1 = new SalesReportRow("Product C", 15, new BigDecimal("1500.00"));
        SalesReportRow row2 = new SalesReportRow("Product D", 8, new BigDecimal("800.00"));

        List<SalesReportRow> expectedList = List.of(row1, row2);

        when(reportDao.getAllSalesReport(startDate, endDate, clientId)).thenReturn(expectedList);

        // Act
        List<SalesReportRow> result = reportApi.getAllSalesReport(startDate, endDate, clientId);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Product C", result.get(0).getProductName());
        assertEquals(15, result.get(0).getQuantitySold());
        assertEquals(new BigDecimal("1500.00"), result.get(0).getRevenue());
        assertEquals("Product D", result.get(1).getProductName());
        assertEquals(8, result.get(1).getQuantitySold());
        assertEquals(new BigDecimal("800.00"), result.get(1).getRevenue());
        verify(reportDao).getAllSalesReport(startDate, endDate, clientId);
    }
}
