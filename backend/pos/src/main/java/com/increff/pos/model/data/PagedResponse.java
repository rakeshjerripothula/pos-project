package com.increff.pos.model.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PagedResponse<T> {

    private List<T> data;
    private Long total;

}
