package com.increff.pos.model.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalesReportRowData {

    private String productName;
    private Integer quantitySold;
    private Double revenue;
}
