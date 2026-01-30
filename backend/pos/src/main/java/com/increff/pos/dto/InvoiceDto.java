package com.increff.pos.dto;

import com.increff.pos.flow.InvoiceFlow;
import com.increff.pos.model.data.InvoiceSummaryData;
import com.increff.pos.util.ConversionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InvoiceDto {

    @Autowired
    private InvoiceFlow invoiceFlow;

    public InvoiceSummaryData generateInvoice(Integer orderId) {
        return ConversionUtil.invoiceEntityToSummaryData(invoiceFlow.generateInvoice(orderId));
    }

    public byte[] downloadInvoice(Integer orderId) {
        return invoiceFlow.downloadInvoice(orderId);
    }

}
