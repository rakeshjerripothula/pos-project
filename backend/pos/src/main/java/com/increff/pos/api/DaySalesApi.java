package com.increff.pos.api;

import com.increff.pos.dao.DaySalesDao;
import com.increff.pos.dao.ReportDao;
import com.increff.pos.entity.DaySalesEntity;
import com.increff.pos.model.internal.DaySalesAggregate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class DaySalesApi {

    @Autowired
    private DaySalesDao daySalesDao;

    @Autowired
    private ReportDao reportDao;

    @Transactional
    public void calculateForDate(LocalDate date) {

        DaySalesAggregate aggregate = reportDao.getDaySalesAggregate(date);

        daySalesDao.deleteByDate(date);

        DaySalesEntity entity = new DaySalesEntity();
        entity.setDate(date);
        entity.setInvoicedOrdersCount(aggregate.getInvoicedOrdersCountAsInt());
        entity.setInvoicedItemsCount(aggregate.getInvoicedItemsCountAsInt());
        entity.setTotalRevenue(
                aggregate.getTotalRevenue()
                        .setScale(2, RoundingMode.HALF_UP)
        );

        daySalesDao.save(entity);
    }

    @Transactional(readOnly = true)
    public Page<DaySalesEntity> findByDateRange(
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        List<DaySalesEntity> results = daySalesDao.findByDateRange(startDate, endDate);

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), results.size());

        if (start >= results.size()) {
            return new PageImpl<>(List.of(), pageable, results.size());
        }

        return new PageImpl<>(
                results.subList(start, end),
                pageable,
                results.size()
        );
    }
}
