package com.increff.pos.model.form;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientSearchForm extends PageForm {

    private String clientName;
    private Boolean enabled;
}
