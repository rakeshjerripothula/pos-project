package com.increff.invoice.model.internal;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
public class InvoiceModel {

    private String invoiceNumber;
    private ZonedDateTime invoiceDate;
    private String clientName;
    private List<InvoiceItemModel> items;
    private BigDecimal totalAmount;

}
