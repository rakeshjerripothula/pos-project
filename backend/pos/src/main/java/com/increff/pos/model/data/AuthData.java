package com.increff.pos.model.data;

import com.increff.pos.domain.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthData {
    private Integer userId;
    private UserRole role;
}
