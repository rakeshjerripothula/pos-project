package com.increff.pos.util;

import com.increff.pos.model.data.DaySalesData;
import com.increff.pos.model.data.SalesReportRowData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CsvExportUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static byte[] exportSalesReportToCsv(List<SalesReportRowData> rows) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            writer.write("Product Name,Quantity Sold,Revenue\n");
            
            for (SalesReportRowData row : rows) {
                writer.write(escapeCsvField(row.getProductName()));
                writer.write(",");
                writer.write(row.getQuantitySold() != null ? row.getQuantitySold().toString() : "0");
                writer.write(",");
                writer.write(row.getRevenue() != null ? String.format("%.2f", row.getRevenue()) : "0.00");
                writer.write("\n");
            }
        }
        
        return outputStream.toByteArray();
    }

    public static byte[] exportDaySalesReportToCsv(List<DaySalesData> rows) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            writer.write("Date,Invoiced Orders Count,Invoiced Items Count,Total Revenue\n");
            
            for (DaySalesData row : rows) {
                writer.write(row.getDate() != null ? row.getDate().format(DATE_FORMATTER) : "");
                writer.write(",");
                writer.write(row.getInvoicedOrdersCount() != null ? row.getInvoicedOrdersCount().toString() : "0");
                writer.write(",");
                writer.write(row.getInvoicedItemsCount() != null ? row.getInvoicedItemsCount().toString() : "0");
                writer.write(",");
                writer.write(row.getTotalRevenue() != null ? row.getTotalRevenue().toString() : "0.00");
                writer.write("\n");
            }
        }
        
        return outputStream.toByteArray();
    }

    private static String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        
        return field;
    }
}
