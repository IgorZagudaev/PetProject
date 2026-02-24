package ru.samara.pet.portfolio_service;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.PostgreSQLContainer;



@Testcontainers
@SpringBootTest
class PortfolioServiceApplicationTests {

    // 🐳 Поднимаем PostgreSQL в Docker для тестов
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("portfolio_test")
            .withUsername("test")
            .withPassword("test");

    // 🔗 Пробрасываем настройки контейнера в Spring Context
    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // Отключаем миграции, если они есть, или используем test profile
        registry.add("spring.flyway.enabled", () -> true);
    }

    @Test
    void contextLoads() {}
}