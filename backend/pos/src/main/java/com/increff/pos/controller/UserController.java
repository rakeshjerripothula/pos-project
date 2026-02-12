package com.increff.pos.controller;

import com.increff.pos.dto.UserDto;
import com.increff.pos.model.data.UserData;
import com.increff.pos.model.form.UserForm;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserDto userDto;

    @PostMapping(path = "/signup")
    public UserData signup(@Valid @RequestBody UserForm form) {
        return userDto.createUser(form);
    }

    @PostMapping(path = "/login")
    public UserData login(@Valid @RequestBody UserForm form) {
        return userDto.login(form);
    }

}
