package com.increff.pos.model.form;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
public class InvoiceForm {

    private String invoiceNumber;
    private ZonedDateTime invoiceDate;
    private String clientName;

    private List<InvoiceItemForm> items;

    private BigDecimal totalAmount;
}