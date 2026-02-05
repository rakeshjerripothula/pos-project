package com.increff.pos.controller;

import com.increff.pos.dto.InventoryDto;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.data.PagedResponse;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.model.form.InventorySearchForm;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public List<InventoryData> uploadInventoryTsv(
            @RequestParam("file") MultipartFile file) {

        return inventoryDto.uploadTsv(file);
    }

}
