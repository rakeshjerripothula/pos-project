package com.increff.pos.dto;

import com.increff.pos.domain.UserRole;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.UserData;
import com.increff.pos.model.form.UserForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserDtoTest {

    @Mock
    private com.increff.pos.api.UserApi userApi;

    @InjectMocks
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createUser_validForm_success() {
        // Arrange
        UserForm form = new UserForm();
        form.setEmail("test@example.com");
        
        com.increff.pos.entity.UserEntity savedUser = createUserEntity(1, "test@example.com", UserRole.OPERATOR);
        when(userApi.signup(any())).thenReturn(savedUser);

        // Act
        UserData result = userDto.createUser(form);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(UserRole.OPERATOR, result.getRole());
        verify(userApi, times(1)).signup(any());
    }

    @Test
    void createUser_invalidEmail_throwsException() {
        // Arrange
        UserForm form = new UserForm();
        form.setEmail("invalid-email"); // Invalid email format

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> userDto.createUser(form));
        assertEquals("BAD_DATA", exception.getStatus().name());
        assertTrue(exception.hasErrors());
        assertTrue(exception.getErrors().get(0).getMessage().contains("Invalid email format"));
        verify(userApi, never()).signup(any());
    }

    @Test
    void createUser_nullForm_throwsException() {
        // Act & Assert
        assertThrows(ApiException.class, () -> userDto.createUser(null));
        verify(userApi, never()).signup(any());
    }

    @Test
    void login_validForm_success() {
        // Arrange
        UserForm form = new UserForm();
        form.setEmail("test@example.com");
        
        com.increff.pos.entity.UserEntity loggedInUser = createUserEntity(1, "test@example.com", UserRole.OPERATOR);
        when(userApi.login(any())).thenReturn(loggedInUser);

        // Act
        UserData result = userDto.login(form);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(UserRole.OPERATOR, result.getRole());
        verify(userApi, times(1)).login(any());
    }

    @Test
    void login_invalidEmail_throwsException() {
        // Arrange
        UserForm form = new UserForm();
        form.setEmail(""); // Empty email

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> userDto.login(form));
        assertEquals("BAD_DATA", exception.getStatus().name());
        assertTrue(exception.hasErrors());
        assertTrue(exception.getErrors().get(0).getMessage().contains("Email is required"));
        verify(userApi, never()).login(any());
    }

    @Test
    void login_nullForm_throwsException() {
        // Act & Assert
        assertThrows(ApiException.class, () -> userDto.login(null));
        verify(userApi, never()).login(any());
    }

    
    
    private com.increff.pos.entity.UserEntity createUserEntity(Integer id, String email, UserRole role) {
        com.increff.pos.entity.UserEntity user = new com.increff.pos.entity.UserEntity();
        user.setId(id);
        user.setEmail(email);
        user.setRole(role);
        return user;
    }
}
