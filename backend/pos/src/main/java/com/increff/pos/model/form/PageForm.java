package com.increff.pos.model.form;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageForm {

    @NotNull
    @Min(0)
    private Integer page;

    @NotNull
    @Min(1)
    @Max(100)
    private Integer pageSize;

}
