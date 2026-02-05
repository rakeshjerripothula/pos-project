package com.increff.pos.model.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TsvUploadError {
    private Integer rowNumber;
    private String[] originalData;
    private String errorMessage;
}
