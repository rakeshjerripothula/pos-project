package com.increff.pos.model.internal;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SalesReportRow {

    private String productName;
    private Integer quantitySold;
    private BigDecimal revenue;

    public SalesReportRow(String productName, Integer quantitySold, BigDecimal revenue) {
        this.productName = productName;
        this.quantitySold = quantitySold;
        this.revenue = revenue;
    }
}
