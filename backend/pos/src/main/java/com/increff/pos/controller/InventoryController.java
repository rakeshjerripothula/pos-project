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
    public void uploadInventoryTsv(@RequestParam("file") MultipartFile file) {
        inventoryDto.uploadTsv(file);
    }

}
