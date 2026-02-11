package com.increff.pos.model.form;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
public class SalesReportForm {

    @NotNull(message = "Start date is required")
    private ZonedDateTime startDate;

    @NotNull(message = "End date is required")
    private ZonedDateTime endDate;

    private Integer clientId;

    private Integer page;

    private Integer pageSize;
}
