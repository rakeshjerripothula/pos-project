package com.increff.invoice.model.form;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class InvoiceItemForm {

    private String productName;
    private Integer quantity;
    private BigDecimal sellingPrice;
    private BigDecimal lineTotal;

}
