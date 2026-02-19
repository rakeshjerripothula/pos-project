package com.increff.pos.api;

import com.increff.pos.dao.UserDao;
import com.increff.pos.model.domain.UserRole;
import com.increff.pos.entity.UserEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserApiTest {

    @Mock
    private UserDao userDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserApi userApi;

    @BeforeEach
    void setUp() {
        userApi = new UserApi("");
        try {
            java.lang.reflect.Field userDaoField = UserApi.class.getDeclaredField("userDao");
            userDaoField.setAccessible(true);
            userDaoField.set(userApi, userDao);
            
            java.lang.reflect.Field passwordEncoderField = UserApi.class.getDeclaredField("passwordEncoder");
            passwordEncoderField.setAccessible(true);
            passwordEncoderField.set(userApi, passwordEncoder);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mock dependencies", e);
        }
    }

    @Test
    void should_signup_user_when_email_not_exists() {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        String normalizedEmail = "test@example.com";
        String hashedPassword = "hashedPassword123";

        when(userDao.selectByEmail(normalizedEmail)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(hashedPassword);
        doAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(1);
            return null;
        }).when(userDao).save(any(UserEntity.class));

        // Act
        UserEntity result = userApi.signup(email, password);

        // Assert
        assertEquals(1, result.getId());
        assertEquals(normalizedEmail, result.getEmail());
        assertEquals(hashedPassword, result.getPassword());
        assertEquals(UserRole.OPERATOR, result.getRole());
        verify(userDao).selectByEmail(normalizedEmail);
        verify(passwordEncoder).encode(password);
        verify(userDao).save(any(UserEntity.class));
    }

    @Test
    void should_signup_supervisor_when_email_in_supervisor_list() {
        // Arrange
        UserApi supervisorApi = new UserApi("supervisor@example.com,admin@example.com");
        // Inject the mock dependencies
        try {
            java.lang.reflect.Field userDaoField = UserApi.class.getDeclaredField("userDao");
            userDaoField.setAccessible(true);
            userDaoField.set(supervisorApi, userDao);
            
            java.lang.reflect.Field passwordEncoderField = UserApi.class.getDeclaredField("passwordEncoder");
            passwordEncoderField.setAccessible(true);
            passwordEncoderField.set(supervisorApi, passwordEncoder);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mock dependencies", e);
        }

        String email = "SUPERVISOR@example.com"; // Test case insensitivity
        String password = "password123";
        String normalizedEmail = "supervisor@example.com";
        String hashedPassword = "hashedPassword123";

        when(userDao.selectByEmail(normalizedEmail)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(hashedPassword);
        doAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(1);
            return null;
        }).when(userDao).save(any(UserEntity.class));

        // Act
        UserEntity result = supervisorApi.signup(email, password);

        // Assert
        assertEquals(1, result.getId());
        assertEquals(normalizedEmail, result.getEmail());
        assertEquals(hashedPassword, result.getPassword());
        assertEquals(UserRole.SUPERVISOR, result.getRole());
        verify(userDao).selectByEmail(normalizedEmail);
        verify(passwordEncoder).encode(password);
        verify(userDao).save(any(UserEntity.class));
    }

    @Test
    void should_throw_exception_when_signup_with_existing_email() {
        // Arrange
        String email = "existing@example.com";
        String password = "password123";
        String normalizedEmail = "existing@example.com";
        UserEntity existingUser = new UserEntity();
        existingUser.setId(1);
        existingUser.setEmail(normalizedEmail);

        when(userDao.selectByEmail(normalizedEmail)).thenReturn(Optional.of(existingUser));

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> userApi.signup(email, password));
        assertEquals(ApiStatus.CONFLICT, exception.getStatus());
        assertEquals("User with this email already exists", exception.getMessage());
        verify(userDao).selectByEmail(normalizedEmail);
        verify(userDao, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void should_normalize_email_during_signup() {
        // Arrange
        String email = "  TEST@EXAMPLE.COM  ";
        String password = "password123";
        String normalizedEmail = "test@example.com";
        String hashedPassword = "hashedPassword123";

        when(userDao.selectByEmail(normalizedEmail)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(hashedPassword);
        doAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(1);
            return null;
        }).when(userDao).save(any(UserEntity.class));

        // Act
        UserEntity result = userApi.signup(email, password);

        // Assert
        assertEquals(normalizedEmail, result.getEmail());
        verify(userDao).selectByEmail(normalizedEmail);
        verify(passwordEncoder).encode(password);
    }


    @Test
    void should_get_user_by_id_when_exists() {
        // Arrange
        UserEntity user = new UserEntity();
        user.setId(1);
        user.setEmail("test@example.com");

        when(userDao.selectById(1)).thenReturn(Optional.of(user));

        // Act
        UserEntity result = userApi.getCheckById(1);

        // Assert
        assertEquals(1, result.getId());
        assertEquals("test@example.com", result.getEmail());
        verify(userDao).selectById(1);
    }

    @Test
    void should_throw_exception_when_getting_user_by_id_not_found() {
        // Arrange
        when(userDao.selectById(1)).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> userApi.getCheckById(1));
        assertEquals(ApiStatus.NOT_FOUND, exception.getStatus());
        assertEquals("User not found: 1", exception.getMessage());
        verify(userDao).selectById(1);
    }


    @Test
    void should_parse_supervisor_emails_with_whitespace_and_empty_values() {
        // Arrange
        UserApi api = new UserApi(" supervisor@example.com , , admin@example.com , ");

        // Act & Assert - Should not throw exception during initialization
        assertNotNull(api);
    }

    @Test
    void should_handle_null_supervisor_emails() {
        // Arrange
        UserApi api = new UserApi(null);

        // Act & Assert - Should not throw exception during initialization
        assertNotNull(api);
    }

    @Test
    void should_handle_empty_supervisor_emails() {
        // Arrange
        UserApi api = new UserApi("");

        // Act & Assert - Should not throw exception during initialization
        assertNotNull(api);
    }

    @Test
    void should_determine_operator_role_when_not_in_supervisor_list() {
        // Arrange
        UserApi api = new UserApi("supervisor@example.com");
        // Inject the mock dependencies
        try {
            java.lang.reflect.Field userDaoField = UserApi.class.getDeclaredField("userDao");
            userDaoField.setAccessible(true);
            userDaoField.set(api, userDao);
            
            java.lang.reflect.Field passwordEncoderField = UserApi.class.getDeclaredField("passwordEncoder");
            passwordEncoderField.setAccessible(true);
            passwordEncoderField.set(api, passwordEncoder);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mock dependencies", e);
        }

        String email = "operator@example.com";
        String password = "password123";
        String hashedPassword = "hashedPassword123";

        when(userDao.selectByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(hashedPassword);
        doAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(1);
            return null;
        }).when(userDao).save(any(UserEntity.class));

        // Act
        UserEntity result = api.signup(email, password);

        // Assert
        assertEquals(UserRole.OPERATOR, result.getRole());
    }

    @Test
    void should_handle_null_email_in_signup() {
        // Arrange
        String password = "password123";
        String hashedPassword = "hashedPassword123";

        when(userDao.selectByEmail(null)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(hashedPassword);
        doAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(1);
            return null;
        }).when(userDao).save(any(UserEntity.class));

        // Act
        UserEntity result = userApi.signup(null, password);

        // Assert
        assertEquals(1, result.getId());
        assertNull(result.getEmail());
        assertEquals(hashedPassword, result.getPassword());
        verify(userDao).selectByEmail(null);
        verify(passwordEncoder).encode(password);
        verify(userDao).save(any(UserEntity.class));
    }

    @Test
    void should_handle_empty_email_in_signup() {
        // Arrange
        String email = "";
        String password = "password123";
        String normalizedEmail = "";
        String hashedPassword = "hashedPassword123";

        when(userDao.selectByEmail(normalizedEmail)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(hashedPassword);
        doAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(1);
            return null;
        }).when(userDao).save(any(UserEntity.class));

        // Act
        UserEntity result = userApi.signup(email, password);

        // Assert
        assertEquals(1, result.getId());
        assertEquals(normalizedEmail, result.getEmail());
        assertEquals(hashedPassword, result.getPassword());
        verify(userDao).selectByEmail(normalizedEmail);
        verify(passwordEncoder).encode(password);
        verify(userDao).save(any(UserEntity.class));
    }

}
