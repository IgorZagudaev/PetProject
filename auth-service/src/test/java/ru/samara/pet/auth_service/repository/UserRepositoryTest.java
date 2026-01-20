package ru.samara.pet.auth_service.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.samara.pet.auth_service.model.User;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Тесты репозитория пользователей")
@Sql("/test-data.sql")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User testUser2;

    @BeforeEach
    void setUp() {
        // Очищаем БД перед каждым тестом
        userRepository.deleteAll();

        // Создаем тестовых пользователей
        testUser = new User();
        testUser.setEmail("user1@example.com");
        testUser.setPasswordHash("hashed_password_1");
        testUser.setRoles(Set.of("ROLE_USER"));

        testUser2 = new User();
        testUser2.setEmail("user2@example.com");
        testUser2.setPasswordHash("hashed_password_2");
        testUser2.setRoles(Set.of("ROLE_USER", "ROLE_ADMIN"));
    }

    @Test
    void findByEmail_ExistingEmail_ReturnsUser() {

        entityManager.persistAndFlush(testUser);

        // when
        Optional<User> found = userRepository.findByEmail("user1@example.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("user1@example.com");
    }

    @Test
    void findByEmail_NonExistingEmail_ReturnsEmpty() {
        // when
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // then
        assertThat(found).isEmpty();
    }

}