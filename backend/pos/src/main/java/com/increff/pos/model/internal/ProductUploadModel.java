package com.increff.pos.model.internal;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductUploadModel {
    private String clientName;
    private String productName;
    private String barcode;
    private BigDecimal mrp;
    private String imageUrl;
}
