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
import java.util.List;

@RestController
@RequestMapping("/inventory")
@CrossOrigin(origins = "http://localhost:3000")
public class InventoryController {

    @Autowired
    private InventoryDto inventoryDto;

    @PostMapping
    public InventoryData upsert(@RequestBody @Valid InventoryForm form) {
        return inventoryDto.upsert(form);
    }

    @PostMapping("/list")
    public PagedResponse<InventoryData> list(@RequestBody @Valid InventorySearchForm form) {
        return inventoryDto.list(form);
    }

    @GetMapping("/{productId}")
    public InventoryData getByProductId(@PathVariable Integer productId) {
        return inventoryDto.getByProductId(productId);
    }

    @PostMapping("/upload/tsv")
    public ResponseEntity<?> uploadInventoryTsv(@RequestParam("file") MultipartFile file) {
        
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
            // Convert ApiException to TSV error format
            TsvUploadError error = new TsvUploadError(
                null, // We don't have row number info from ApiException
                new String[]{}, // Empty original data
                e.getMessage()
            );
            
            try {
                byte[] errorData = TsvErrorExportUtil.exportErrorsToTsv(List.of(error), "inventory");
                String filename = TsvErrorExportUtil.generateErrorFilename("inventory");
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", filename);
                headers.setContentLength(errorData.length);
                
                return new ResponseEntity<>(errorData, headers, HttpStatus.BAD_REQUEST);
            } catch (IOException ioException) {
                return new ResponseEntity<>("Failed to process error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (IOException e) {
            return new ResponseEntity<>("Failed to process file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
