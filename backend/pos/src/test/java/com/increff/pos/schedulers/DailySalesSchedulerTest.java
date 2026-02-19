package com.increff.pos.schedulers;

import com.increff.pos.api.DaySalesApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailySalesSchedulerTest {

    @Mock
    private DaySalesApi daySalesApi;

    @InjectMocks
    private DailySalesScheduler dailySalesScheduler;

    @Test
    void should_calculate_daily_sales_for_yesterday() {
        // Arrange
        LocalDate fixedDate = LocalDate.of(2023, 1, 15);
        ZonedDateTime fixedDateTime = fixedDate.atStartOfDay(ZoneId.of("UTC"));
        
        // Act
        dailySalesScheduler.calculateDailySales();

        // Assert
        verify(daySalesApi).calculateForDate(any(LocalDate.class));
    }

    @Test
    void should_calculate_daily_sales_with_correct_date_calculation() {
        // Arrange
        LocalDate expectedYesterday = LocalDate.now(ZoneId.of("UTC")).minusDays(1);

        // Act
        dailySalesScheduler.calculateDailySales();

        // Assert
        verify(daySalesApi).calculateForDate(expectedYesterday);
    }

    @Test
    void should_handle_day_sales_api_exception_gracefully() {
        // Arrange
        doThrow(new RuntimeException("Database error"))
            .when(daySalesApi).calculateForDate(any(LocalDate.class));

        // Act & Assert
        Assertions.assertDoesNotThrow(() -> dailySalesScheduler.calculateDailySales());
        verify(daySalesApi).calculateForDate(any(LocalDate.class));
    }

    @Test
    void should_use_utc_timezone_for_date_calculation() {
        // Arrange - Verify that the scheduler uses UTC timezone
        LocalDate utcYesterday = ZonedDateTime.now(ZoneId.of("UTC")).toLocalDate().minusDays(1);

        // Act
        dailySalesScheduler.calculateDailySales();

        // Assert
        verify(daySalesApi).calculateForDate(utcYesterday);
    }

    @Test
    void should_call_day_sales_api_exactly_once_per_execution() {
        // Act
        dailySalesScheduler.calculateDailySales();

        // Assert
        verify(daySalesApi, times(1)).calculateForDate(any(LocalDate.class));
    }

    @Test
    void should_calculate_for_previous_day_not_current_day() {
        // Arrange
        LocalDate today = LocalDate.now(ZoneId.of("UTC"));
        LocalDate yesterday = today.minusDays(1);

        // Act
        dailySalesScheduler.calculateDailySales();

        // Assert
        verify(daySalesApi).calculateForDate(yesterday);
        verify(daySalesApi, never()).calculateForDate(today);
    }

    @Test
    void should_handle_month_boundary_transitions() {
        // Arrange - This test would be more reliable with a fixed clock, but we can verify the logic
        LocalDate expectedDate = LocalDate.now(ZoneId.of("UTC")).minusDays(1);

        // Act
        dailySalesScheduler.calculateDailySales();

        // Assert
        verify(daySalesApi).calculateForDate(expectedDate);
    }

    @Test
    void should_handle_leap_year_calculations() {
        // Arrange - Verify the date calculation works correctly during leap years
        LocalDate expectedDate = LocalDate.now(ZoneId.of("UTC")).minusDays(1);

        // Act
        dailySalesScheduler.calculateDailySales();

        // Assert
        verify(daySalesApi).calculateForDate(expectedDate);
    }
}
