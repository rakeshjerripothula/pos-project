package com.increff.pos.model.internal;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DaySalesAggregate {

    private Long invoicedOrdersCount;
    private Long invoicedItemsCount;
    private BigDecimal totalRevenue;

    public DaySalesAggregate(Long invoicedOrdersCount, Long invoicedItemsCount, BigDecimal totalRevenue) {
        this.invoicedOrdersCount = invoicedOrdersCount;
        this.invoicedItemsCount = invoicedItemsCount;
        this.totalRevenue = totalRevenue;
    }

    public Integer getInvoicedOrdersCountAsInt() {
        return invoicedOrdersCount.intValue();
    }

    public Integer getInvoicedItemsCountAsInt() {
        return invoicedItemsCount.intValue();
    }

}
