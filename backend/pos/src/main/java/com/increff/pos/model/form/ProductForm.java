package com.increff.pos.model.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductForm {

    @NotBlank(message = "Product name is required")
    private String productName;

    @NotNull(message = "MRP is required")
    @Min(value = 0, message = "MRP cannot be negative")
    private BigDecimal mrp;

    @NotNull(message = "Client ID is required")
    private Integer clientId;

    @NotBlank(message = "Barcode is required")
    private String barcode;

    private String imageUrl;
}
