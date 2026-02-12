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
import com.increff.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class ReportDto extends AbstractDto{

    @Autowired
    private ReportApi reportApi;

    @Autowired
    private DaySalesApi daySalesApi;

    public DaySalesPageData getDaySales(DaySalesReportForm form) {
        checkValid(form);

        var startDate = form.getStartDate();
        var endDate = form.getEndDate();

        ValidationUtil.validateOptionalDateRange(startDate, endDate);

        Integer page = Objects.nonNull(form.getPage()) ? form.getPage() : 0;
        Integer pageSize = Objects.nonNull(form.getPageSize()) ? form.getPageSize() : 10;

        Pageable pageable = PageRequest.of(page, pageSize);

        Page<DaySalesEntity> pageResult = daySalesApi.findByDateRange(startDate, endDate, pageable);

        List<DaySalesData> rows = pageResult.getContent().stream().map(ConversionUtil::daySalesEntityToData).toList();

        DaySalesPageData response = new DaySalesPageData();
        response.setContent(rows);
        response.setPage(page);
        response.setPageSize(pageSize);
        response.setTotalElements(pageResult.getTotalElements());

        return response;
    }

    public SalesReportPageData getSalesReport(SalesReportForm form) {
        checkValid(form);

        ZonedDateTime startDate = form.getStartDate();
        ZonedDateTime endDate = form.getEndDate();

        ValidationUtil.validateOptionalDateRange(startDate, endDate);

        Integer page = Objects.nonNull(form.getPage()) ? form.getPage() : 0;
        Integer pageSize = Objects.nonNull(form.getPageSize()) ? form.getPageSize() : 10;

        Pageable pageable = PageRequest.of(page, pageSize);

        Page<SalesReportRow> pageResult = reportApi.getSalesReport(startDate, endDate, form.getClientId(), pageable);

        SalesReportPageData response = new SalesReportPageData();
        response.setRows(pageResult.getContent().stream().map(ConversionUtil::salesReportRowToData).toList());
        response.setPage(page);
        response.setPageSize(pageSize);
        response.setTotalElements(pageResult.getTotalElements());

        return response;
    }

    public List<SalesReportRowData> getAllSalesReportForExport(SalesReportForm form) {
        checkValid(form);

        ZonedDateTime startDate = form.getStartDate();
        ZonedDateTime endDate = form.getEndDate();

        ValidationUtil.validateOptionalDateRange(startDate, endDate);

        List<SalesReportRow> allRows = reportApi.getAllSalesReport(startDate, endDate, form.getClientId());

        return allRows.stream().map(ConversionUtil::salesReportRowToData).toList();
    }

    public List<DaySalesData> getAllDaySalesForExport(DaySalesReportForm form) {
        checkValid(form);

        ValidationUtil.validateOptionalDateRange(form.getStartDate(), form.getEndDate());

        List<DaySalesEntity> entities = daySalesApi.findAllByDateRange(form.getStartDate(), form.getEndDate());

        return entities.stream().map(ConversionUtil::daySalesEntityToData).toList();
    }

}
