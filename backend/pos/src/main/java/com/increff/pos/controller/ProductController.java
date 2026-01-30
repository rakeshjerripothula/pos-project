package com.increff.pos.controller;

import com.increff.pos.dto.ProductDto;
import com.increff.pos.model.data.ProductData;
import com.increff.pos.model.form.ProductForm;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/products")
@CrossOrigin(origins = "http://localhost:3000")
public class ProductController {

    @Autowired
    private ProductDto productDto;

    @PostMapping
    public ProductData add(@Valid @RequestBody ProductForm productForm) {
        return productDto.createProduct(productForm);
    }

    @PutMapping("/{id}")
    public ProductData update(
            @PathVariable Integer id,
            @Valid @RequestBody ProductForm productForm) {

        return productDto.updateProduct(id, productForm);
    }

    @GetMapping
    public List<ProductData> getAll(
            @RequestParam(required = false) Integer clientId,
            @RequestParam(required = false) String barcode,
            @RequestParam(required = false) String name) {

        return productDto.getAllProducts();
    }

    @GetMapping("/{id}")
    public ProductData getById(@PathVariable Integer id) {
        return productDto.getById(id);
    }

    @PostMapping("/bulk")
    public List<ProductData> bulkCreate(
            @RequestBody @Valid List<ProductForm> forms) {
        return productDto.bulkCreateProducts(forms);
    }

    @PostMapping("/upload/tsv")
    public List<ProductData> uploadProductTsv(
            @RequestParam("file") MultipartFile file) {

        return productDto.uploadProductsTsv(file);
    }

}
