package com.increff.pos.controller;

import com.increff.pos.dto.InventoryDto;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.data.PagedResponse;
import com.increff.pos.model.data.TsvUploadError;
import com.increff.pos.model.data.TsvUploadResult;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.model.form.InventorySearchForm;
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
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryDto inventoryDto;

    @GetMapping
    public List<InventoryData> getAll() {
        return inventoryDto.getAll();
    }

    @PostMapping
    public InventoryData upsert(@RequestBody @Valid InventoryForm form) {
        return inventoryDto.upsert(form);
    }

    @PostMapping("/list")
    public PagedResponse<InventoryData> list(@RequestBody @Valid InventorySearchForm form) {
        return inventoryDto.list(form);
    }

    @PostMapping("/upload/tsv")
    public ResponseEntity<?> uploadInventoryTsv(@RequestParam("file") MultipartFile file) throws IOException {

        try {
            TsvUploadResult<InventoryData> result = inventoryDto.uploadTsv(file);

            if (!result.isSuccess()) {
                byte[] errorData = TsvErrorExportUtil.exportErrorsToTsv(result.getErrors(), "inventory");
                String filename = TsvErrorExportUtil.generateErrorFilename("inventory");

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", filename);
                headers.setContentLength(errorData.length);

                return new ResponseEntity<>(errorData, headers, HttpStatus.BAD_REQUEST);
            }

            return ResponseEntity.ok(result.getData());
        } catch (ApiException e) {
            // Convert ApiException to TSV error file
            List<TsvUploadError> errors = Collections.singletonList(
                    new TsvUploadError(null, null, e.getMessage())
            );
            byte[] errorData = TsvErrorExportUtil.exportErrorsToTsv(errors, "inventory");
            String filename = TsvErrorExportUtil.generateErrorFilename("inventory");

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

}
