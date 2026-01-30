package com.increff.pos.controller;

import com.increff.pos.dto.UserDto;
import com.increff.pos.model.data.UserData;
import com.increff.pos.model.form.UserForm;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired
    private UserDto userDto;

    @PostMapping(path = "/signup", consumes = "application/json", produces = "application/json")
    public UserData signup(@Valid @RequestBody UserForm form) {
        return userDto.createUser(form);
    }

    @PostMapping(path = "/login", consumes = "application/json", produces = "application/json")
    public UserData login(@Valid @RequestBody UserForm form) {
        return userDto.login(form);
    }

    @GetMapping("/{id}")
    public UserData getById(@PathVariable Integer id) {
        return userDto.getById(id);
    }

    @GetMapping
    public UserData getByEmail(@RequestParam String email) {
        return userDto.getByEmail(email);
    }
}
