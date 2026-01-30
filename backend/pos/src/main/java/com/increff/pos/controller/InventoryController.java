package com.increff.pos.controller;

import com.increff.pos.dto.InventoryDto;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.form.InventoryForm;
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

    @GetMapping
    public List<InventoryData> getAll() {
        return inventoryDto.list();
    }

    @GetMapping("/{productId}")
    public InventoryData getByProductId(@PathVariable Integer productId) {
        return inventoryDto.getByProductId(productId);
    }

    @PostMapping("/bulk")
    public List<InventoryData> bulkUpsert(
            @RequestBody @Valid List<InventoryForm> forms) {
        return inventoryDto.bulkUpsert(forms);
    }
//
    @PostMapping("/upload/tsv")
    public List<InventoryData> uploadInventoryTsv(
            @RequestParam("file") MultipartFile file) {

        return inventoryDto.uploadTsv(file);
    }

}
