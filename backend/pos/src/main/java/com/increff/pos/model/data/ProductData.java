package com.increff.pos.model.data;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductData {

    private Integer id;
    private String productName;
    private BigDecimal mrp;
    private Integer clientId;
    private String clientName;
    private String barcode;
    private String imageUrl;

}
