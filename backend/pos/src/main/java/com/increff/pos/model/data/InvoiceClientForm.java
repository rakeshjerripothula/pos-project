package com.increff.pos.model.data;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class InvoiceClientForm {

    private Integer orderId;
    private String clientName;

    private List<InvoiceItemData> items;

    private BigDecimal totalAmount;
}