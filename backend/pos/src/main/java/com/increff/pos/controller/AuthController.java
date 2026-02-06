package com.increff.pos.controller;

import com.increff.pos.dto.AuthDto;
import com.increff.pos.model.data.AuthData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private AuthDto authDto;

    @GetMapping("/check")
    public AuthData check(@RequestParam Integer userId) {
        return authDto.check(userId);
    }
}
