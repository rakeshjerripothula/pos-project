package com.increff.pos.model.internal;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DaySalesAggregate {

    private final Long invoicedOrdersCount;
    private final Long invoicedItemsCount;
    private final BigDecimal totalRevenue;

    public DaySalesAggregate(Long invoicedOrdersCount, Long invoicedItemsCount, BigDecimal totalRevenue) {
        this.invoicedOrdersCount = invoicedOrdersCount != null ? invoicedOrdersCount : 0L;
        this.invoicedItemsCount = invoicedItemsCount != null ? invoicedItemsCount : 0L;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }

    public int getInvoicedOrdersCountAsInt() {
        return invoicedOrdersCount.intValue();
    }

    public int getInvoicedItemsCountAsInt() {
        return invoicedItemsCount.intValue();
    }
}
