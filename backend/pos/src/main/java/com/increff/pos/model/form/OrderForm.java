package com.increff.pos.model.form;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderForm {

    @NotEmpty(message = "Order must contain at least one item")
    private List<OrderItemForm> items;
}
