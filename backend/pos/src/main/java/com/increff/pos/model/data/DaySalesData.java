package com.increff.pos.model.data;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class DaySalesData {

    private LocalDate date;
    private Integer invoicedOrdersCount;
    private Integer invoicedItemsCount;
    private BigDecimal totalRevenue;
}
