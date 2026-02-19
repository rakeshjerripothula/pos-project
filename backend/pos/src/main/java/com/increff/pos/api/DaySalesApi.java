package com.increff.pos.api;

import com.increff.pos.dao.DaySalesDao;
import com.increff.pos.dao.ReportDao;
import com.increff.pos.entity.DaySalesEntity;
import com.increff.pos.model.internal.DaySalesAggregate;
import com.increff.pos.util.ConversionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Transactional
@Service
public class DaySalesApi {

    @Autowired
    private DaySalesDao daySalesDao;

    @Autowired
    private ReportDao reportDao;

    public void calculateForDate(LocalDate date) {

        ZoneId ist = ZoneId.of("Asia/Kolkata");
        ZoneId utc = ZoneId.of("UTC");

        ZonedDateTime istStart = date.atStartOfDay(ist);
        ZonedDateTime istEnd = istStart.plusDays(1);

        ZonedDateTime utcStart = istStart.withZoneSameInstant(utc);
        ZonedDateTime utcEnd = istEnd.withZoneSameInstant(utc);

        DaySalesAggregate aggregate = reportDao.selectDaySalesByDate(utcStart, utcEnd);

        DaySalesEntity entity = ConversionUtil.daySalesAggregateToEntity(date, aggregate);
        daySalesDao.save(entity);
    }

    @Transactional(readOnly = true)
    public Page<DaySalesEntity> findByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {

        return daySalesDao.selectByDateRange(startDate, endDate, pageable);
    }

    public List<DaySalesEntity> findAllByDateRange(LocalDate startDate, LocalDate endDate) {

        return daySalesDao.selectAllByDateRange(startDate, endDate);
    }
}
