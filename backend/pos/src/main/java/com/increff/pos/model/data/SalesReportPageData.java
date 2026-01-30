package com.increff.pos.model.data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SalesReportPageData {

    private List<SalesReportRowData> rows;

    private Integer page;
    private Integer pageSize;
    private Long totalElements;
}
