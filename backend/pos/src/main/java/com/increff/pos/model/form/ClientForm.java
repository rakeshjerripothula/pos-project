package com.increff.pos.model.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientForm {

    @NotBlank(message = "Client name cannot be empty")
    private String clientName;
}
