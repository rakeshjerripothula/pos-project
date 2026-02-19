package com.increff.invoice.model.form;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class InvoiceForm {

    @NotNull(message = "Order id cannot be null")
    private Integer orderId;
    
    @NotBlank(message = "Client name cannot be blank")
    private String clientName;
    
    @NotNull(message = "Items list cannot be null")
    private List<InvoiceItemForm> items;
    
    @NotNull(message = "Total amount cannot be null")
    private BigDecimal totalAmount;
}