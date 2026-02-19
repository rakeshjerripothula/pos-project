package com.increff.pos.api;

import com.increff.pos.dao.ReportDao;
import com.increff.pos.model.internal.SalesReportRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReportApi {

    @Autowired
    private ReportDao reportDao;

    public Page<SalesReportRow> getSalesReport(ZonedDateTime startDate, ZonedDateTime endDate, Integer clientId,
            Pageable pageable) {
        return reportDao.selectByFiltersSalesPage(startDate, endDate, clientId, pageable);
    }

    public List<SalesReportRow> getAllSalesReport(ZonedDateTime startDate, ZonedDateTime endDate, Integer clientId) {
        return reportDao.selectAllSalesReport(startDate, endDate, clientId);
    }
}

