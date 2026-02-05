package com.increff.pos.api;

import com.increff.pos.dao.UserDao;
import com.increff.pos.domain.UserRole;
import com.increff.pos.entity.UserEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserApiTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserApi userApi;

    @BeforeEach
    void setUp() {
        // Create a new instance with empty supervisor emails for most tests
        userApi = new UserApi("");
    }

    @Test
    void should_signup_user_when_email_not_exists() {
        // Arrange
        String email = "test@example.com";
        String normalizedEmail = "test@example.com";

        when(userDao.findByEmail(normalizedEmail)).thenReturn(Optional.empty());
        doAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(1);
            return null;
        }).when(userDao).insert(any(UserEntity.class));

        // Act
        UserEntity result = userApi.signup(email);

        // Assert
        assertEquals(1, result.getId());
        assertEquals(normalizedEmail, result.getEmail());
        assertEquals(UserRole.OPERATOR, result.getRole());
        verify(userDao).findByEmail(normalizedEmail);
        verify(userDao).insert(any(UserEntity.class));
    }

    @Test
    void should_signup_supervisor_when_email_in_supervisor_list() {
        // Arrange
        UserApi supervisorApi = new UserApi("supervisor@example.com,admin@example.com");
        String email = "SUPERVISOR@example.com"; // Test case insensitivity
        String normalizedEmail = "supervisor@example.com";

        when(userDao.findByEmail(normalizedEmail)).thenReturn(Optional.empty());
        doAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(1);
            return null;
        }).when(userDao).insert(any(UserEntity.class));

        // Act
        UserEntity result = supervisorApi.signup(email);

        // Assert
        assertEquals(1, result.getId());
        assertEquals(normalizedEmail, result.getEmail());
        assertEquals(UserRole.SUPERVISOR, result.getRole());
        verify(userDao).findByEmail(normalizedEmail);
        verify(userDao).insert(any(UserEntity.class));
    }

    @Test
    void should_throw_exception_when_signup_with_existing_email() {
        // Arrange
        String email = "existing@example.com";
        String normalizedEmail = "existing@example.com";
        UserEntity existingUser = new UserEntity();
        existingUser.setId(1);
        existingUser.setEmail(normalizedEmail);

        when(userDao.findByEmail(normalizedEmail)).thenReturn(Optional.of(existingUser));

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> userApi.signup(email));
        assertEquals(ApiStatus.CONFLICT, exception.getStatus());
        assertEquals("User with this email already exists", exception.getMessage());
        verify(userDao).findByEmail(normalizedEmail);
        verify(userDao, never()).insert(any());
    }

    @Test
    void should_normalize_email_during_signup() {
        // Arrange
        String email = "  TEST@EXAMPLE.COM  ";
        String normalizedEmail = "test@example.com";

        when(userDao.findByEmail(normalizedEmail)).thenReturn(Optional.empty());
        doAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(1);
            return null;
        }).when(userDao).insert(any(UserEntity.class));

        // Act
        UserEntity result = userApi.signup(email);

        // Assert
        assertEquals(normalizedEmail, result.getEmail());
        verify(userDao).findByEmail(normalizedEmail);
    }

    @Test
    void should_login_when_email_exists() {
        // Arrange
        String email = "test@example.com";
        String normalizedEmail = "test@example.com";
        UserEntity user = new UserEntity();
        user.setId(1);
        user.setEmail(normalizedEmail);
        user.setRole(UserRole.OPERATOR);

        when(userDao.findByEmail(normalizedEmail)).thenReturn(Optional.of(user));

        // Act
        UserEntity result = userApi.login(email);

        // Assert
        assertEquals(1, result.getId());
        assertEquals(normalizedEmail, result.getEmail());
        assertEquals(UserRole.OPERATOR, result.getRole());
        verify(userDao).findByEmail(normalizedEmail);
    }

    @Test
    void should_throw_exception_when_login_with_nonexistent_email() {
        // Arrange
        String email = "nonexistent@example.com";
        String normalizedEmail = "nonexistent@example.com";

        when(userDao.findByEmail(normalizedEmail)).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> userApi.login(email));
        assertEquals(ApiStatus.NOT_FOUND, exception.getStatus());
        assertEquals("User not found", exception.getMessage());
        verify(userDao).findByEmail(normalizedEmail);
    }

    @Test
    void should_normalize_email_during_login() {
        // Arrange
        String email = "  TEST@EXAMPLE.COM  ";
        String normalizedEmail = "test@example.com";
        UserEntity user = new UserEntity();
        user.setId(1);
        user.setEmail(normalizedEmail);

        when(userDao.findByEmail(normalizedEmail)).thenReturn(Optional.of(user));

        // Act
        UserEntity result = userApi.login(email);

        // Assert
        assertEquals(normalizedEmail, result.getEmail());
        verify(userDao).findByEmail(normalizedEmail);
    }

    @Test
    void should_get_user_by_id_when_exists() {
        // Arrange
        UserEntity user = new UserEntity();
        user.setId(1);
        user.setEmail("test@example.com");

        when(userDao.findById(1)).thenReturn(Optional.of(user));

        // Act
        UserEntity result = userApi.getById(1);

        // Assert
        assertEquals(1, result.getId());
        assertEquals("test@example.com", result.getEmail());
        verify(userDao).findById(1);
    }

    @Test
    void should_throw_exception_when_getting_user_by_id_not_found() {
        // Arrange
        when(userDao.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> userApi.getById(1));
        assertEquals(ApiStatus.NOT_FOUND, exception.getStatus());
        assertEquals("User not found: 1", exception.getMessage());
        verify(userDao).findById(1);
    }

    @Test
    void should_get_user_by_email_when_exists() {
        // Arrange
        String email = "test@example.com";
        String normalizedEmail = "test@example.com";
        UserEntity user = new UserEntity();
        user.setId(1);
        user.setEmail(normalizedEmail);

        when(userDao.findByEmail(normalizedEmail)).thenReturn(Optional.of(user));

        // Act
        UserEntity result = userApi.getByEmail(email);

        // Assert
        assertEquals(1, result.getId());
        assertEquals(normalizedEmail, result.getEmail());
        verify(userDao).findByEmail(normalizedEmail);
    }

    @Test
    void should_throw_exception_when_getting_user_by_email_not_found() {
        // Arrange
        String email = "nonexistent@example.com";
        String normalizedEmail = "nonexistent@example.com";

        when(userDao.findByEmail(normalizedEmail)).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> userApi.getByEmail(email));
        assertEquals(ApiStatus.NOT_FOUND, exception.getStatus());
        assertEquals("User not found", exception.getMessage());
        verify(userDao).findByEmail(normalizedEmail);
    }

    @Test
    void should_normalize_email_when_getting_by_email() {
        // Arrange
        String email = "  TEST@EXAMPLE.COM  ";
        String normalizedEmail = "test@example.com";
        UserEntity user = new UserEntity();
        user.setId(1);
        user.setEmail(normalizedEmail);

        when(userDao.findByEmail(normalizedEmail)).thenReturn(Optional.of(user));

        // Act
        UserEntity result = userApi.getByEmail(email);

        // Assert
        assertEquals(normalizedEmail, result.getEmail());
        verify(userDao).findByEmail(normalizedEmail);
    }

    @Test
    void should_validate_supervisor_when_user_is_supervisor() {
        // Arrange
        UserEntity supervisor = new UserEntity();
        supervisor.setId(1);
        supervisor.setEmail("supervisor@example.com");
        supervisor.setRole(UserRole.SUPERVISOR);

        when(userDao.findById(1)).thenReturn(Optional.of(supervisor));

        // Act & Assert
        assertDoesNotThrow(() -> userApi.validateSupervisor(1));
        verify(userDao).findById(1);
    }

    @Test
    void should_throw_exception_when_validating_supervisor_for_operator() {
        // Arrange
        UserEntity operator = new UserEntity();
        operator.setId(1);
        operator.setEmail("operator@example.com");
        operator.setRole(UserRole.OPERATOR);

        when(userDao.findById(1)).thenReturn(Optional.of(operator));

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> userApi.validateSupervisor(1));
        assertEquals(ApiStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("Access denied", exception.getMessage());
        verify(userDao).findById(1);
    }

    @Test
    void should_throw_exception_when_validating_supervisor_for_nonexistent_user() {
        // Arrange
        when(userDao.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> userApi.validateSupervisor(1));
        assertEquals(ApiStatus.NOT_FOUND, exception.getStatus());
        assertEquals("User not found: 1", exception.getMessage());
        verify(userDao).findById(1);
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
        String email = "operator@example.com";

        when(userDao.findByEmail(email)).thenReturn(Optional.empty());
        doAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(1);
            return null;
        }).when(userDao).insert(any(UserEntity.class));

        // Act
        UserEntity result = api.signup(email);

        // Assert
        assertEquals(UserRole.OPERATOR, result.getRole());
    }
}
