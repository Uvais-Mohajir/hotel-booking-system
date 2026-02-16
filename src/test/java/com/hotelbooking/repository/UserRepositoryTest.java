package com.hotelbooking.repository;

import com.hotelbooking.entity.User;
import com.hotelbooking.enums.Role;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmailShouldReturnUserWhenPresent() {
        User user = User.builder()
                .email("user@test.com")
                .password("pass")
                .fullName("Test User")
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();
        entityManager.persist(user);
        entityManager.flush();

        Optional<User> found = userRepository.findByEmail("user@test.com");

        assertTrue(found.isPresent());
        assertEquals("Test User", found.get().getFullName());
    }

    @Test
    void existsByEmailShouldReturnTrueOnlyForExistingEmail() {
        User user = User.builder()
                .email("existing@test.com")
                .password("pass")
                .fullName("Existing User")
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();
        entityManager.persist(user);
        entityManager.flush();

        assertTrue(userRepository.existsByEmail("existing@test.com"));
        assertFalse(userRepository.existsByEmail("missing@test.com"));
    }
}
