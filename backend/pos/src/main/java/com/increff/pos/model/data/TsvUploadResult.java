package com.increff.pos.model.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TsvUploadResult<T> {
    private boolean success;
    private List<T> data;
    private List<TsvUploadError> errors;
    
    public static <T> TsvUploadResult<T> success(List<T> data) {
        return new TsvUploadResult<>(true, data, null);
    }
    
    public static <T> TsvUploadResult<T> failure(List<TsvUploadError> errors) {
        return new TsvUploadResult<>(false, null, errors);
    }
}
