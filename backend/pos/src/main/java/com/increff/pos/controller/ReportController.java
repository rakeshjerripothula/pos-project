package com.increff.pos.controller;

import com.increff.pos.dto.ReportDto;
import com.increff.pos.model.data.DaySalesPageData;
import com.increff.pos.model.data.SalesReportPageData;
import com.increff.pos.model.data.SalesReportRowData;
import com.increff.pos.model.form.DaySalesReportForm;
import com.increff.pos.model.form.SalesReportForm;
import com.increff.pos.util.CsvExportUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/reports")
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

    @PostMapping("/sales/export")
    public ResponseEntity<byte[]> exportSalesReport(@RequestBody SalesReportForm form) {
        try {
            List<SalesReportRowData> allRows = reportDto.getAllSalesReportForExport(form);
            byte[] csvData = CsvExportUtil.exportSalesReportToCsv(allRows);
            
            String filename = "sales-report-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".csv";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(csvData.length);
            
            return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/day-sales/export")
    public ResponseEntity<byte[]> exportDaySalesReport(@RequestBody @Valid DaySalesReportForm form) {
        try {
            DaySalesPageData reportData = reportDto.getDaySales(form);
            byte[] csvData = CsvExportUtil.exportDaySalesReportToCsv(reportData.getContent());
            
            String filename = "day-sales-report-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".csv";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(csvData.length);
            
            return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
