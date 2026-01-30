package com.increff.pos.dto;

import com.increff.pos.api.UserApi;
import com.increff.pos.entity.UserEntity;
import com.increff.pos.model.data.AuthData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthDto {

    @Autowired
    private UserApi userApi;

    public AuthData check(Integer userId) {
        UserEntity user = userApi.getById(userId);

        AuthData data = new AuthData();
        data.setUserId(user.getId());
        data.setRole(user.getRole());
        return data;
    }
}
