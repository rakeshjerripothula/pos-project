package com.increff.pos.dao;

import com.increff.pos.entity.UserEntity;
import com.increff.pos.model.domain.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class UserDaoIntegrationTest {

    @Autowired
    private UserDao userDao;

    @Test
    void testInsert() {
        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");
        user.setRole(UserRole.OPERATOR);

        userDao.save(user);

        Optional<UserEntity> found = userDao.selectById(user.getId());
        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
        assertEquals(UserRole.OPERATOR, found.get().getRole());
    }

    @Test
    void testSelectById() {
        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");
        user.setRole(UserRole.SUPERVISOR);
        userDao.save(user);

        Optional<UserEntity> found = userDao.selectById(user.getId());

        assertTrue(found.isPresent());
        assertEquals(user.getId(), found.get().getId());
        assertEquals("test@example.com", found.get().getEmail());
        assertEquals(UserRole.SUPERVISOR, found.get().getRole());
    }

    @Test
    void testSelectByEmail() {
        UserEntity user = new UserEntity();
        user.setEmail("unique@example.com");
        user.setPassword("password123"); // Add required password
        user.setRole(UserRole.OPERATOR);
        userDao.save(user);

        Optional<UserEntity> found = userDao.selectByEmail("unique@example.com");

        assertTrue(found.isPresent());
        assertEquals("unique@example.com", found.get().getEmail());
        assertEquals(UserRole.OPERATOR, found.get().getRole());

        Optional<UserEntity> notFound = userDao.selectByEmail("nonexistent@example.com");
        assertFalse(notFound.isPresent());
    }
}
