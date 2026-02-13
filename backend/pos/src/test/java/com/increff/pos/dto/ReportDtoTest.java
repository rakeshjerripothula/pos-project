package com.increff.pos.dto;

import com.increff.pos.api.DaySalesApi;
import com.increff.pos.api.ReportApi;
import com.increff.pos.entity.DaySalesEntity;
import com.increff.pos.model.data.DaySalesData;
import com.increff.pos.model.data.DaySalesPageData;
import com.increff.pos.model.data.SalesReportPageData;
import com.increff.pos.model.data.SalesReportRowData;
import com.increff.pos.model.form.DaySalesReportForm;
import com.increff.pos.model.form.SalesReportForm;
import com.increff.pos.model.internal.SalesReportRow;
import com.increff.pos.util.ConversionUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportDtoTest {

    @Mock
    private ReportApi reportApi;

    @Mock
    private DaySalesApi daySalesApi;

    @InjectMocks
    private ReportDto reportDto;

    @Test
    void should_get_day_sales_report() {
        // Arrange
        DaySalesReportForm form = new DaySalesReportForm();
        form.setStartDate(LocalDate.of(2024, 1, 1));
        form.setEndDate(LocalDate.of(2024, 1, 31));
        form.setPage(0);
        form.setPageSize(10);

        DaySalesEntity entity = new DaySalesEntity();
        entity.setDate(LocalDate.of(2024, 1, 15));
        entity.setTotalRevenue(BigDecimal.valueOf(1000.0));

        Page<DaySalesEntity> pageResult = new PageImpl<>(List.of(entity));

        when(daySalesApi.findByDateRange(any(), any(), any())).thenReturn(pageResult);

        // Act
        DaySalesPageData result = reportDto.getDaySales(form);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(0, result.getPage());
        assertEquals(10, result.getPageSize());
        assertEquals(1, result.getTotalElements());
        verify(daySalesApi).findByDateRange(any(), any(), any());
    }

    @Test
    void should_get_sales_report() {
        // Arrange
        SalesReportForm form = new SalesReportForm();
        form.setStartDate(ZonedDateTime.parse("2024-01-01T00:00:00Z"));
        form.setEndDate(ZonedDateTime.parse("2024-01-31T23:59:59Z"));
        form.setClientId(1);
        form.setPage(0);
        form.setPageSize(10);

        SalesReportRow row = new SalesReportRow("Test Product", 10, BigDecimal.valueOf(1000.0));
        Page<SalesReportRow> pageResult = new PageImpl<>(List.of(row));

        when(reportApi.getSalesReport(any(), any(), any(), any())).thenReturn(pageResult);

        // Act
        SalesReportPageData result = reportDto.getSalesReport(form);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getRows().size());
        assertEquals(0, result.getPage());
        assertEquals(10, result.getPageSize());
        assertEquals(1, result.getTotalElements());
        verify(reportApi).getSalesReport(any(), any(), any(), any());
    }

    @Test
    void should_get_all_sales_report_for_export() {
        // Arrange
        SalesReportForm form = new SalesReportForm();
        form.setStartDate(ZonedDateTime.parse("2024-01-01T00:00:00Z"));
        form.setEndDate(ZonedDateTime.parse("2024-01-31T23:59:59Z"));
        form.setClientId(1);

        SalesReportRow row = new SalesReportRow("Test Product", 10, BigDecimal.valueOf(1000.0));
        List<SalesReportRow> allRows = List.of(row);

        when(reportApi.getAllSalesReport(any(), any(), any())).thenReturn(allRows);

        // Act
        List<SalesReportRowData> result = reportDto.getAllSalesReportForExport(form);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getProductName());
        assertEquals(10, result.get(0).getQuantitySold());
        assertEquals(1000.0, result.get(0).getRevenue());
        verify(reportApi).getAllSalesReport(any(), any(), any());
    }

    @Test
    void should_get_all_day_sales_for_export() {
        // Arrange
        DaySalesReportForm form = new DaySalesReportForm();
        form.setStartDate(LocalDate.of(2024, 1, 1));
        form.setEndDate(LocalDate.of(2024, 1, 31));

        DaySalesEntity entity = new DaySalesEntity();
        entity.setDate(LocalDate.of(2024, 1, 15));
        entity.setTotalRevenue(BigDecimal.valueOf(1000.0));

        List<DaySalesEntity> entities = List.of(entity);

        when(daySalesApi.findAllByDateRange(any(), any())).thenReturn(entities);

        // Act
        List<DaySalesData> result = reportDto.getAllDaySalesForExport(form);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(LocalDate.of(2024, 1, 15), result.get(0).getDate());
        assertEquals(BigDecimal.valueOf(1000.0), result.get(0).getTotalRevenue());
        verify(daySalesApi).findAllByDateRange(any(), any());
    }
}
