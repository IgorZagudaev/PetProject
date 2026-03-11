package ru.samara.pet.auth_service.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.samara.pet.auth_service.model.User;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@Testcontainers
@ContextConfiguration(initializers = {UserRepositoryIT.Initializer.class})
@DisplayName("Тесты репозитория пользователей с PostgreSQL")
@Sql("/test-data.sql")
class UserRepositoryIT {

    @Container
    public static PostgreSQLContainer<?> postgresqlContainer = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User testUser2;

    // Инициализатор для динамической настройки datasource
    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgresqlContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgresqlContainer.getUsername(),
                    "spring.datasource.password=" + postgresqlContainer.getPassword(),
                    "spring.datasource.driver-class-name=org.postgresql.Driver",
                    "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
                    "spring.jpa.hibernate.ddl-auto=create-drop",
                    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect",
                    "spring.test.database.replace=none" // Отключаем замену на H2
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

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
    @DisplayName("Поиск пользователя по существующему email должен вернуть пользователя")
    void findByEmail_ExistingEmail_ReturnsUser() {
        // given
        entityManager.persistAndFlush(testUser);

        // when
        Optional<User> found = userRepository.findByEmail("user1@example.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("user1@example.com");
    }

    @Test
    @DisplayName("Поиск пользователя по несуществующему email должен вернуть пустой Optional")
    void findByEmail_NonExistingEmail_ReturnsEmpty() {
        // when
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Сохранение пользователя с ролями должно работать корректно")
    void saveUser_WithRoles_ShouldPersistRoles() {
        // given
        Set<String> roles = Set.of("ROLE_USER", "ROLE_ADMIN");
        testUser.setRoles(roles);

        // when
        User savedUser = entityManager.persistAndFlush(testUser);
        entityManager.clear(); // Очищаем контекст чтобы загрузить заново

        Optional<User> found = userRepository.findByEmail(savedUser.getEmail());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getRoles()).isEqualTo(roles);
    }
}