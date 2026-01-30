package com.increff.pos.model.data;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemData {

    private Integer productId;
    private String productName;
    private Integer quantity;
    private BigDecimal sellingPrice;
}
