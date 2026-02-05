package com.increff.pos.controller;

import com.increff.pos.dto.ClientDto;
import com.increff.pos.model.data.ClientData;
import com.increff.pos.model.data.PagedResponse;
import com.increff.pos.model.form.ClientForm;
import com.increff.pos.model.form.ClientSearchForm;
import com.increff.pos.model.form.ClientToggleForm;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
@CrossOrigin(origins = "http://localhost:3000")
public class ClientController {

    @Autowired
    private ClientDto clientDto;

    @PostMapping
    public ClientData create(@RequestBody @Valid ClientForm form) {
        return clientDto.createClient(form);
    }

    @GetMapping
    public List<ClientData> getAll() {
        return clientDto.getAll();
    }

    @PutMapping("/{id}")
    public ClientData update(@PathVariable Integer id, @RequestBody @Valid ClientForm form) {
        return clientDto.updateClient(id, form);
    }

    @PatchMapping("/{id}/toggle")
    public ClientData toggle(@PathVariable Integer id, @RequestBody @Valid ClientToggleForm form) {
        return clientDto.toggleClient(id, form);
    }

    @PostMapping("/list")
    public PagedResponse<ClientData> list(@RequestBody @Valid ClientSearchForm form) {
        return clientDto.getAllClients(form);
    }

}
