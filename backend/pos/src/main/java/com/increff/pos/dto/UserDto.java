package com.increff.pos.dto;

import com.increff.pos.api.UserApi;
import com.increff.pos.entity.UserEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import com.increff.pos.model.data.UserData;
import com.increff.pos.model.form.UserForm;
import com.increff.pos.util.ConversionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserDto extends AbstractDto {

    @Autowired
    private UserApi userApi;

    public UserData createUser(UserForm form) {
        checkValid(form);

        UserEntity user = userApi.signup(form.getEmail(), form.getPassword());
        return ConversionUtil.userEntityToData(user);
    }

    @PreAuthorize("hasAnyRole('OPERATOR','SUPERVISOR')")
    public UserData getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        UserEntity user = userApi.getByEmail(email);

        return ConversionUtil.userEntityToData(user);
    }

}
