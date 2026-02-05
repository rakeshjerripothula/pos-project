package com.increff.pos.model.form;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientToggleForm {
    
    @NotNull(message = "Enabled status is required")
    private Boolean enabled;
}
