package com.increff.pos.controller;

import com.increff.pos.dto.ReportDto;
import com.increff.pos.model.data.DaySalesData;
import com.increff.pos.model.data.DaySalesPageData;
import com.increff.pos.model.data.SalesReportPageData;
import com.increff.pos.model.data.SalesReportRowData;
import com.increff.pos.model.form.DaySalesReportForm;
import com.increff.pos.model.form.SalesReportForm;
import com.increff.pos.util.CsvExportUtil;
import jakarta.validation.Valid;
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

    private final ReportDto reportDto;

    public ReportController(ReportDto reportDto) {
        this.reportDto = reportDto;
    }

    @PostMapping("/sales")
    public SalesReportPageData getSalesReport(@RequestBody @Valid SalesReportForm form) {
        return reportDto.getSalesReport(form);
    }

    @PostMapping("/sales/export")
    public ResponseEntity<byte[]> exportSalesReport(@RequestBody @Valid SalesReportForm form) throws IOException {

        List<SalesReportRowData> rows = reportDto.getAllSalesReportForExport(form);
        byte[] csvData = CsvExportUtil.exportSalesReportToCsv(rows);

        String filename = buildFilename("sales-report");

        return buildCsvResponse(csvData, filename);
    }

    @PostMapping("/day-sales")
    public DaySalesPageData getDaySalesReport(@RequestBody @Valid DaySalesReportForm form) {
        return reportDto.getDaySales(form);
    }

    @PostMapping("/day-sales/export")
    public ResponseEntity<byte[]> exportDaySalesReport(@RequestBody @Valid DaySalesReportForm form) throws IOException {

        List<DaySalesData> rows = reportDto.getAllDaySalesForExport(form);
        byte[] csvData = CsvExportUtil.exportDaySalesReportToCsv(rows);

        String filename = buildFilename("day-sales-report");

        return buildCsvResponse(csvData, filename);
    }

    private ResponseEntity<byte[]> buildCsvResponse(byte[] data, String filename) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(data.length);

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    private String buildFilename(String prefix) {
        return prefix + "-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".csv";
    }
}
