package com.increff.pos.dto;

import com.increff.pos.model.domain.UserRole;
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
        form.setPassword("password123");

        com.increff.pos.entity.UserEntity savedUser = createUserEntity(1, "test@example.com", UserRole.OPERATOR);
        when(userApi.signup(any(), any())).thenReturn(savedUser);

        // Act
        UserData result = userDto.createUser(form);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(UserRole.OPERATOR, result.getRole());
        verify(userApi, times(1)).signup("test@example.com", "password123");
    }

    @Test
    void createUser_invalidEmail_throwsException() {
        // Arrange
        UserForm form = new UserForm();
        form.setEmail("invalid-email"); // Invalid email format
        form.setPassword("password123");

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> userDto.createUser(form));
        assertEquals("BAD_REQUEST", exception.getStatus().name());
        assertTrue(exception.hasErrors());
        assertTrue(exception.getErrors().get(0).getMessage().contains("Invalid email format"));
        verify(userApi, never()).signup(any(), any());
    }

    @Test
    void createUser_nullForm_throwsException() {
        // Act & Assert
        assertThrows(ApiException.class, () -> userDto.createUser(null));
        verify(userApi, never()).signup(any(), any());
    }

    @Test
    void createUser_nullEmail_throwsException() {
        // Arrange
        UserForm form = new UserForm();
        form.setEmail(null);
        form.setPassword("password123");

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> userDto.createUser(form));
        assertEquals("BAD_REQUEST", exception.getStatus().name());
        assertTrue(exception.hasErrors());
        verify(userApi, never()).signup(any(), any());
    }

    @Test
    void createUser_nullPassword_throwsException() {
        // Arrange
        UserForm form = new UserForm();
        form.setEmail("test@example.com");
        form.setPassword(null);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> userDto.createUser(form));
        assertEquals("BAD_REQUEST", exception.getStatus().name());
        assertTrue(exception.hasErrors());
        verify(userApi, never()).signup(any(), any());
    }

    @Test
    void createUser_supervisorEmail_success() {
        // Arrange
        UserForm form = new UserForm();
        form.setEmail("supervisor@example.com");
        form.setPassword("password123");

        com.increff.pos.entity.UserEntity savedUser = createUserEntity(1, "supervisor@example.com", UserRole.SUPERVISOR);
        when(userApi.signup(any(), any())).thenReturn(savedUser);

        // Act
        UserData result = userDto.createUser(form);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("supervisor@example.com", result.getEmail());
        assertEquals(UserRole.SUPERVISOR, result.getRole());
        verify(userApi, times(1)).signup("supervisor@example.com", "password123");
    }




    private com.increff.pos.entity.UserEntity createUserEntity(Integer id, String email, UserRole role) {
        com.increff.pos.entity.UserEntity user = new com.increff.pos.entity.UserEntity();
        user.setId(id);
        user.setEmail(email);
        user.setRole(role);
        return user;
    }
}
