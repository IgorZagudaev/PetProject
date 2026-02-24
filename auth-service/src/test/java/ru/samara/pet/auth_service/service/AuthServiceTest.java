package ru.samara.pet.auth_service.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.samara.pet.auth_service.model.Outbox;
import ru.samara.pet.auth_service.model.User;
import ru.samara.pet.auth_service.model.dto.AuthResponse;
import ru.samara.pet.auth_service.model.dto.LoginRequest;
import ru.samara.pet.auth_service.model.dto.RegisterRequest;
import ru.samara.pet.auth_service.model.dto.UserRegistered;
import ru.samara.pet.auth_service.repository.OutboxRepository;
import ru.samara.pet.auth_service.repository.UserRepository;
import ru.samara.pet.security.JwtUtil;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService тесты")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private OutboxRepository outboxRepository;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("Регистрация пользователя")
    class RegisterTests {

        @Test
        @DisplayName("Успешная регистрация создаёт пользователя и событие Outbox")
        void register_success_createsUserAndOutboxEvent() {
            // given
            var email = "test@example.com";
            var password = "password123";
            var encodedPassword = "encoded_password";
            var userId = UUID.randomUUID();

            var request = new RegisterRequest(email, password);
            var savedUser = new User();
            savedUser.setId(userId);
            savedUser.setEmail(email);
            savedUser.setPasswordHash(encodedPassword);

            given(userRepository.findByEmail(email)).willReturn(Optional.empty());
            given(passwordEncoder.encode(password)).willReturn(encodedPassword);
            given(userRepository.save(any(User.class))).willReturn(savedUser);

            // when
            authService.register(request);

            // then
            verify(userRepository).findByEmail(email);
            verify(passwordEncoder).encode(password);
            verify(userRepository).save(argThat(user ->
                    user.getEmail().equals(email) &&
                    user.getPasswordHash().equals(encodedPassword)
            ));

            // Проверяем сохранение события Outbox
            ArgumentCaptor<Outbox> outboxCaptor = ArgumentCaptor.forClass(Outbox.class);
            verify(outboxRepository).save(outboxCaptor.capture());
            Outbox capturedOutbox = outboxCaptor.getValue();

            assertThat(capturedOutbox.getAggregateId()).isEqualTo(userId);
            assertThat(capturedOutbox.getAggregateType()).isEqualTo("auth service");
            assertThat(capturedOutbox.getEventType()).isEqualTo("Registered user");
            assertThat(capturedOutbox.getBody()).isInstanceOf(UserRegistered.class);
            assertThat(((UserRegistered) capturedOutbox.getBody()).uuid()).isEqualTo(userId);
        }

        @Test
        @DisplayName("Регистрация с существующим email выбрасывает исключение")
        void register_emailAlreadyExists_throwsException() {
            // given
            var email = "existing@example.com";
            var request = new RegisterRequest(email, "password123");

            given(userRepository.findByEmail(email))
                    .willReturn(Optional.of(new User()));

            // when & then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Пользователь с таким email уже существует");

            verify(userRepository).findByEmail(email);
            verify(passwordEncoder, never()).encode(any());
            verify(userRepository, never()).save(any());
            verify(outboxRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Авторизация пользователя")
    class LoginTests {

        @Test
        @DisplayName("Успешный вход возвращает JWT-токен")
        void login_success_returnsAuthResponse() {
            // given
            var email = "test@example.com";
            var password = "password123";
            var encodedPassword = "encoded_password";
            var userId = UUID.randomUUID();
            var token = "jwt.token.here";
            var roles = Set.of("ROLE_USER");

            var user = new User();
            user.setId(userId);
            user.setEmail(email);
            user.setPasswordHash(encodedPassword);
            user.setRoles(roles);

            var request = new LoginRequest(email, password);

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(password, encodedPassword)).willReturn(true);
            given(jwtUtil.generateToken(eq(userId.toString()), any())).willReturn(token);

            // when
            AuthResponse response = authService.login(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.token()).isEqualTo(token);

            verify(userRepository).findByEmail(email);
            verify(passwordEncoder).matches(password, encodedPassword);
            verify(jwtUtil).generateToken(eq(userId.toString()), any());
        }

        @Test
        @DisplayName("Вход с несуществующим email выбрасывает исключение")
        void login_userNotFound_throwsException() {
            // given
            var email = "notfound@example.com";
            var request = new LoginRequest(email, "password123");

            given(userRepository.findByEmail(email)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Неверные учетные данные");

            verify(userRepository).findByEmail(email);
            verify(passwordEncoder, never()).matches(any(), any());
            verify(jwtUtil, never()).generateToken(any(), any());
        }

        @Test
        @DisplayName("Вход с неверным паролем выбрасывает исключение")
        void login_wrongPassword_throwsException() {
            // given
            var email = "test@example.com";
            var rawPassword = "wrong_password";
            var encodedPassword = "encoded_correct_password";
            var userId = UUID.randomUUID();

            var user = new User();
            user.setId(userId);
            user.setEmail(email);
            user.setPasswordHash(encodedPassword);

            var request = new LoginRequest(email, rawPassword);

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(rawPassword, encodedPassword)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Неверные учетные данные");

            verify(userRepository).findByEmail(email);
            verify(passwordEncoder).matches(rawPassword, encodedPassword);
            verify(jwtUtil, never()).generateToken(any(), any());
        }
    }
}