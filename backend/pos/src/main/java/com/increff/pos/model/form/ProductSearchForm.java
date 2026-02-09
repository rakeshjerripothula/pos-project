package com.increff.pos.model.form;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSearchForm extends PageForm {

    private Integer clientId;
    private String barcode;
    private String productName;
}
