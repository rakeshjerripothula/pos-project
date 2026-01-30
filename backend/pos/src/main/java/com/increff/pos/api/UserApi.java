package com.increff.pos.api;

import com.increff.pos.dao.UserDao;
import com.increff.pos.domain.UserRole;
import com.increff.pos.entity.UserEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserApi {

    @Autowired
    private UserDao userDao;

    private final Set<String> supervisorEmails;

    public UserApi(@Value("${app.supervisor.emails:}") String supervisorEmailsStr) {
        this.supervisorEmails = Arrays.stream(supervisorEmailsStr.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    public UserEntity signup(String email) {
        String normalizedEmail = normalizeEmail(email);

        userDao.findByEmail(normalizedEmail).ifPresent(u -> {
            throw new ApiException(
                    ApiStatus.CONFLICT,
                    "User with this email already exists",
                    "email",
                    "User with this email already exists"
            );
        });

        UserEntity user = new UserEntity();
        user.setEmail(normalizedEmail);
        user.setRole(determineRole(normalizedEmail));

        userDao.insert(user);
        return user;
    }

    public UserEntity login(String email) {
        String normalizedEmail = normalizeEmail(email);
        return userDao.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ApiException(
                        ApiStatus.NOT_FOUND,
                        "User not found",
                        "email",
                        "User not found"
                ));
    }

    public UserEntity getById(Integer userId) {
        return userDao.findById(userId)
                .orElseThrow(() -> new ApiException(
                        ApiStatus.NOT_FOUND,
                        "User not found: " + userId,
                        "userId",
                        "User not found"
                ));
    }

    public UserEntity getByEmail(String email) {
        return userDao.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new ApiException(
                        ApiStatus.NOT_FOUND,
                        "User not found",
                        "email",
                        "User not found"
                ));
    }

    public void validateSupervisor(Integer userId) {
        UserEntity user = getById(userId);
        if (user.getRole() != UserRole.SUPERVISOR) {
            throw new ApiException(
                    ApiStatus.UNAUTHORIZED,
                    "Access denied",
                    "role",
                    "Supervisor access required"
            );
        }
    }

    private UserRole determineRole(String email) {
        return supervisorEmails.contains(email)
                ? UserRole.SUPERVISOR
                : UserRole.OPERATOR;
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
