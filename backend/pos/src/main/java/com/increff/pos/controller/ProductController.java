package com.increff.pos.controller;

import com.increff.pos.dto.ProductDto;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.PagedResponse;
import com.increff.pos.model.data.ProductData;
import com.increff.pos.model.data.TsvUploadError;
import com.increff.pos.model.data.TsvUploadResult;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.model.form.ProductSearchForm;
import com.increff.pos.util.TsvErrorExportUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductDto productDto;

    @GetMapping
    public List<ProductData> getAll() {
        return productDto.getAll();
    }

    @PostMapping
    public ProductData add(@RequestBody @Valid ProductForm productForm) {
        return productDto.createProduct(productForm);
    }

    @PostMapping("/list")
    public PagedResponse<ProductData> list(@RequestBody @Valid ProductSearchForm form) {
        return productDto.listProducts(form);
    }

    @PostMapping("/upload/tsv")
    public void uploadProductTsv(@RequestParam("file") MultipartFile file) {
        productDto.uploadProductsTsv(file);
    }

    @PutMapping("/{id}")
    public ProductData update(@PathVariable Integer id, @Valid @RequestBody ProductForm productForm) {
        return productDto.updateProduct(id, productForm);
    }

}
