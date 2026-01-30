package com.increff.pos.service;

import com.increff.pos.api.DaySalesApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class DailySalesScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DailySalesScheduler.class);

    @Autowired
    private DaySalesApi daySalesApi;

    @Scheduled(cron = "0 0 1 * * ?")
    public void calculateDailySales() {
        LocalDate yesterdayUtc = ZonedDateTime
                .now(ZoneId.of("UTC"))
                .toLocalDate()
                .minusDays(1);

        logger.info("Starting day-on-day sales calculation for {}", yesterdayUtc);

        daySalesApi.calculateForDate(yesterdayUtc);

        logger.info("Completed day-on-day sales calculation for {}", yesterdayUtc);
    }

    public void recalculateHistoricalData(LocalDate startDate, LocalDate endDate) {
        logger.info("Starting historical day sales recalculation from {} to {}", startDate, endDate);

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            daySalesApi.calculateForDate(currentDate);
            currentDate = currentDate.plusDays(1);
        }

        logger.info("Completed historical day sales recalculation");
    }
}
