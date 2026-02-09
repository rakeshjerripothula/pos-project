package com.increff.pos.dto;

import com.increff.pos.domain.UserRole;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthDtoTest {

    @Mock
    private com.increff.pos.api.UserApi userApi;

    @InjectMocks
    private AuthDto authDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void check_validUserId_success() {
        // Arrange
        Integer userId = 1;
        com.increff.pos.entity.UserEntity user = createUserEntity(userId, "test@example.com", UserRole.OPERATOR);
        when(userApi.getById(userId)).thenReturn(user);

        // Act
        AuthData result = authDto.check(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(UserRole.OPERATOR, result.getRole());
        verify(userApi, times(1)).getById(userId);
    }

    @Test
    void check_nullUserId_throwsException() {
        // Act & Assert
        assertThrows(RuntimeException.class, () -> authDto.check(null));
        verify(userApi, times(1)).getById(null);
    }

    @Test
    void check_nonExistentUserId_throwsException() {
        // Arrange
        Integer userId = 999;
        when(userApi.getById(userId)).thenThrow(new RuntimeException("User not found"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authDto.check(userId));
        verify(userApi, times(1)).getById(userId);
    }

    private com.increff.pos.entity.UserEntity createUserEntity(Integer id, String email, UserRole role) {
        com.increff.pos.entity.UserEntity user = new com.increff.pos.entity.UserEntity();
        user.setId(id);
        user.setEmail(email);
        user.setRole(role);
        return user;
    }
}
