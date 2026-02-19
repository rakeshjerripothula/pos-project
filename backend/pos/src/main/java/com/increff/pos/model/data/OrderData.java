package com.increff.pos.model.data;

import com.increff.pos.model.domain.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
public class OrderData {

    private Integer orderId;
    private Integer clientId;
    private ZonedDateTime createdAt;
    private OrderStatus status;
    private List<OrderItemData> items;
}
