package com.increff.pos.controller;

import com.increff.pos.dto.ReportDto;
import com.increff.pos.model.data.DaySalesPageData;
import com.increff.pos.model.data.SalesReportPageData;
import com.increff.pos.model.form.DaySalesReportForm;
import com.increff.pos.model.form.SalesReportForm;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
@CrossOrigin(origins = "http://localhost:3000")
public class ReportController {

    @Autowired
    private ReportDto reportDto;

    @PostMapping("/day-sales")
    public DaySalesPageData getDaySalesReport(@RequestBody @Valid DaySalesReportForm form) {
        return reportDto.getDaySales(form);
    }

    @PostMapping("/sales")
    public SalesReportPageData getSalesReport(@RequestBody @Valid SalesReportForm form) {
        return reportDto.getSalesReport(form);
    }

}
