package com.increff.pos.api;

import com.increff.pos.dao.UserDao;
import com.increff.pos.model.domain.UserRole;
import com.increff.pos.entity.UserEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final Set<String> supervisorEmails;

    public UserApi(@Value("${app.supervisor.emails:}") String supervisorEmailsStr) {
        if (supervisorEmailsStr == null) {
            supervisorEmailsStr = "";
        }
        this.supervisorEmails = Arrays.stream(supervisorEmailsStr.split(",")).map(String::trim)
                .map(String::toLowerCase).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
    }

    public UserEntity signup(String email, String rawPassword) {

        String normalizedEmail = normalizeEmail(email);

        userDao.selectByEmail(normalizedEmail).ifPresent(u -> {
            throw new ApiException(
                    ApiStatus.CONFLICT, "User with this email already exists",
                    "email", "User with this email already exists"
            );
        });

        UserEntity user = new UserEntity();
        user.setEmail(normalizedEmail);
        user.setRole(determineRole(normalizedEmail));

        String hashedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(hashedPassword);

        userDao.save(user);
        return user;
    }

    public UserEntity getCheckById(Integer userId) {
        return userDao.selectById(userId)
                .orElseThrow(() -> new ApiException(
                        ApiStatus.NOT_FOUND, "User not found: " + userId, "userId", "User not found"
                ));
    }

    public UserEntity getCheckByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);

        return userDao.selectByEmail(normalizedEmail)
                .orElseThrow(() -> new ApiException(ApiStatus.NOT_FOUND, "User not found", "email", "User not found"));
    }

    private UserRole determineRole(String email) {
        return supervisorEmails.contains(email) ? UserRole.SUPERVISOR : UserRole.OPERATOR;
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
