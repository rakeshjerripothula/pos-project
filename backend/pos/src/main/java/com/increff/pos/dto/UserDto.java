package com.increff.pos.dto;

import com.increff.pos.api.UserApi;
import com.increff.pos.entity.UserEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import com.increff.pos.model.data.UserData;
import com.increff.pos.model.form.UserForm;
import com.increff.pos.util.ConversionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserDto extends AbstractDto {

    @Autowired
    private UserApi userApi;

    public UserData createUser(UserForm form) {
        checkValid(form);

        UserEntity user = userApi.signup(form.getEmail());
        return ConversionUtil.userEntityToData(user);
    }

    public UserData login(UserForm form) {
        checkValid(form);

        UserEntity user = userApi.login(form.getEmail());
        return ConversionUtil.userEntityToData(user);
    }

    public UserData getById(Integer userId) {
        if(Objects.isNull(userId)){
            throw new ApiException(ApiStatus.BAD_DATA, "User ID is required");
        }
        UserEntity user = userApi.getById(userId);
        return ConversionUtil.userEntityToData(user);
    }

    public UserData getByEmail(String email) {
        if(Objects.isNull(email)){
            throw new ApiException(ApiStatus.BAD_DATA, "Email is required");
        }
        UserEntity user = userApi.getByEmail(email);
        return ConversionUtil.userEntityToData(user);
    }
}
