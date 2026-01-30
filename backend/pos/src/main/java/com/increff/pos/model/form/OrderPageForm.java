package com.increff.pos.model.form;

import com.increff.pos.domain.OrderStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderPageForm {

    private OrderStatus status;
    private Integer clientId;

    private String startDate;
    private String endDate;

    private Integer page = 0;
    private Integer pageSize = 10;
}
