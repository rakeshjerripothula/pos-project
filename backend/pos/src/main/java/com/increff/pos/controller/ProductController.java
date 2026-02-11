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

    @GetMapping("/{id}")
    public ProductData getById(@PathVariable Integer id) {
        return productDto.getById(id);
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
    public ResponseEntity<byte[]> uploadProductTsv(@RequestParam("file") MultipartFile file) throws IOException {

        try {
            TsvUploadResult<ProductData> result = productDto.uploadProductsTsv(file);

            if (!result.isSuccess()) {
                byte[] errorData = TsvErrorExportUtil.exportErrorsToTsv(result.getErrors(), "products");
                String filename = TsvErrorExportUtil.generateErrorFilename("products");

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", filename);
                headers.setContentLength(errorData.length);

                return new ResponseEntity<>(errorData, headers, HttpStatus.BAD_REQUEST);
            }

            return ResponseEntity.ok().build();
        } catch (ApiException e) {
            // Convert ApiException to TSV error file
            List<TsvUploadError> errors = Collections.singletonList(
                    new TsvUploadError(null, null, e.getMessage())
            );
            byte[] errorData = TsvErrorExportUtil.exportErrorsToTsv(errors, "products");
            String filename = TsvErrorExportUtil.generateErrorFilename("products");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(errorData.length);

            HttpStatus httpStatus = switch (e.getStatus()) {
                case CONFLICT -> HttpStatus.CONFLICT;
                case FORBIDDEN -> HttpStatus.FORBIDDEN;
                case NOT_FOUND -> HttpStatus.NOT_FOUND;
                case BAD_DATA, BAD_REQUEST -> HttpStatus.BAD_REQUEST;
                default -> HttpStatus.BAD_REQUEST;
            };

            return new ResponseEntity<>(errorData, headers, httpStatus);
        }
    }

    @PutMapping("/{id}")
    public ProductData update(@PathVariable Integer id, @Valid @RequestBody ProductForm productForm) {

        return productDto.updateProduct(id, productForm);
    }

}
