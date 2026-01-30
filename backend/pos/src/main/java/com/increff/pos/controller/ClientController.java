package com.increff.pos.controller;

import com.increff.pos.dto.ClientDto;
import com.increff.pos.model.data.ClientData;
import com.increff.pos.model.form.ClientForm;
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

    @PutMapping("/{id}")
    public ClientData update(@PathVariable Integer id, @RequestBody @Valid ClientForm form) {
        return clientDto.updateClient(id, form);
    }

    @PatchMapping("/client/{id}/toggle")
    public ClientData toggle(@PathVariable Integer id) {
        return clientDto.toggleClient(id);
    }

    @GetMapping
    public List<ClientData> getAll() {
        return clientDto.getAllClients();
    }

    @PostMapping("/bulk")
    public List<ClientData> bulkCreate(@RequestBody @Valid List<ClientForm> forms) {
        return clientDto.bulkCreate(forms);
    }

}
