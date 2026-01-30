package com.increff.pos.model.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryForm {

    @NotNull(message = "Product ID is required")
    private Integer productId;

    @NotNull
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;
}
