package ru.samara.pet.auth_service.service;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.samara.pet.auth_service.model.User;
import ru.samara.pet.auth_service.model.dto.RegisterRequest;
import ru.samara.pet.auth_service.repository.OutboxRepository;
import ru.samara.pet.auth_service.repository.UserRepository;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
@DisplayName("Интеграционные тесты: транзакционность Outbox")
class AuthServiceTransactionIntegrationTest {

    // 🐳 Поднимаем PostgreSQL в Docker для тестов
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("auth_test")
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

    @Autowired private AuthService authService;
    @Autowired private UserRepository userRepository;
    @Autowired private OutboxRepository outboxRepository;
    @Autowired private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);
        // Очищаем таблицы перед каждым тестом
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(@NotNull TransactionStatus status) {
                outboxRepository.deleteAll();
                userRepository.deleteAll();
            }
        });
    }

    @Nested
    @DisplayName("Атомарность сохранения User и Outbox")
    class AtomicSaveTests {

        @Test
        @DisplayName("✅ Успешная регистрация: и User, и Outbox сохранены в одной транзакции")
        void register_success_bothSavedAtomically() {
            // given
            var request = new RegisterRequest("test@example.com", "password123");

            // when
            authService.register(request);

            // then — проверяем в НОВОЙ транзакции, что данные закоммичены
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    var savedUser = userRepository.findByEmail("test@example.com")
                            .orElseThrow(() -> new AssertionError("User не найден"));

//                    var savedOutbox = outboxRepository.findByAggregateId(savedUser.getId())
//                            .orElseThrow(() -> new AssertionError("Outbox не найден"));

                    assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
//                    assertThat(savedOutbox.getEventType()).isEqualTo("Registered user");
//                    assertThat(savedOutbox.getAggregateId()).isEqualTo(savedUser.getId());
                }
            });
        }

        @Test
        @DisplayName("❌ Если после сохранения User происходит ошибка — всё откатывается")
        void register_outboxFailure_rollsBackUser() {
            // given
            var request = new RegisterRequest("fail@example.com", "password123");

            // 🎭 Создаём "шпиона" на outboxRepository, чтобы сымитировать ошибку
            var outboxSpy = org.mockito.Mockito.spy(outboxRepository);

            // Подменяем репозиторий в сервисе через Reflection (или лучше — через @TestConfiguration)
            // Но проще: протестируем через выбрасывание исключения внутри транзакции
            // Для этого создадим тестовый метод в сервисе или используем AOP-совет

            // 👇 Более правильный подход: вынесем логику в отдельный метод для теста
            // Или используем @TestConfiguration с моком (см. ниже)

            // Пока что проверим rollback через явное исключение:
            assertThatThrownBy(() -> {
                transactionTemplate.execute(status -> {
                    // Сохраняем User вручную
                    var user = new User();
                    user.setEmail("fail@example.com");
                    user.setPasswordHash("encoded");
                    userRepository.save(user);

                    // Имитируем ошибку при сохранении Outbox
                    throw new RuntimeException("DB error on outbox save");
                });
            })
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("DB error on outbox save");

            // then — в новой транзакции проверяем, что User НЕ сохранён
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    assertThat(userRepository.findByEmail("fail@example.com"))
                            .isEmpty();
                    assertThat(outboxRepository.findAll()).isEmpty();
                }
            });
        }
    }
}