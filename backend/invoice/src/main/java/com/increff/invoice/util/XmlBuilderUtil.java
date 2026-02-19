package com.increff.invoice.util;

import com.increff.invoice.model.internal.InvoiceItemModel;
import com.increff.invoice.model.internal.InvoiceModel;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class XmlBuilderUtil {

    public String buildInvoiceXml(InvoiceModel invoiceModel) {
        StringBuilder xml = new StringBuilder();

        xml.append("<invoice>");
        xml.append("<invoiceNumber>").append(invoiceModel.getInvoiceNumber()).append("</invoiceNumber>");
        xml.append("<invoiceDate>").append(formatInvoiceDate(invoiceModel.getInvoiceDate())).append("</invoiceDate>");
        xml.append("<clientName>").append(invoiceModel.getClientName()).append("</clientName>");

        xml.append("<items>");
        for (InvoiceItemModel item : invoiceModel.getItems()) {
            xml.append("<item>");
            xml.append("<productName>").append(item.getProductName()).append("</productName>");
            xml.append("<quantity>").append(item.getQuantity()).append("</quantity>");
            xml.append("<sellingPrice>").append(item.getSellingPrice()).append("</sellingPrice>");
            xml.append("<lineTotal>").append(item.getLineTotal()).append("</lineTotal>");
            xml.append("</item>");
        }
        xml.append("</items>");

        xml.append("<totalAmount>").append(invoiceModel.getTotalAmount()).append("</totalAmount>");
        xml.append("</invoice>");

        return xml.toString();
    }

    private String formatInvoiceDate(ZonedDateTime dateTime) {
        return dateTime
                .withZoneSameInstant(ZoneId.of("Asia/Kolkata"))
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
    }

}
