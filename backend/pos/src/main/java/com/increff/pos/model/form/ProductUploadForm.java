package com.increff.pos.model.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductUploadForm {

    @NotBlank(message = "Product name is required")
    private String productName;

    @NotNull(message = "MRP is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "MRP must be greater than 0")
    private BigDecimal mrp;

    @NotBlank(message = "Client name is required")
    private String clientName;

    @NotBlank(message = "Barcode is required")
    private String barcode;

    private String imageUrl;
}
