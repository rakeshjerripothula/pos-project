package com.increff.invoice.model.internal;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class InvoiceItemModel {

    private String productName;
    private Integer quantity;
    private BigDecimal sellingPrice;
    private BigDecimal lineTotal;

}
