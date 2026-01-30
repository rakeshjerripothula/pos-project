package com.increff.pos.model.data;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
public class InvoiceSummaryData {
    private Integer orderId;
    private ZonedDateTime createdAt;
}
