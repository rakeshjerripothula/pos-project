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

    @Scheduled(cron = "0 0 0 * * ?")
    public void calculateDailySales() {

        ZoneId ist = ZoneId.of("Asia/Kolkata");
        LocalDate yesterdayIst = ZonedDateTime.now(ist)
                .toLocalDate()
                .minusDays(1);

        logger.info("Calculating day sales for IST date {}", yesterdayIst);

        try {
            daySalesApi.calculateForDate(yesterdayIst);
        } catch (Exception e) {
            logger.error(
                    "Failed to calculate day sales for IST date {}", yesterdayIst, e
            );
        }
    }


}
