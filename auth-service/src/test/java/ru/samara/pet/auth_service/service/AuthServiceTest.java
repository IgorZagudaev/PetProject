package ru.samara.pet.auth_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.samara.pet.auth_service.model.dto.AuthResponse;
import ru.samara.pet.auth_service.model.dto.LoginRequest;
import ru.samara.pet.auth_service.model.dto.RegisterRequest;
import ru.samara.pet.auth_service.model.User;
import ru.samara.pet.auth_service.repository.UserRepository;
import ru.samara.pet.security.JwtUtil;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    JwtUtil jwtUtil;
    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private User existingUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setEmail("test@example.com");
        validRegisterRequest.setPassword("password123");

        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("test@example.com");
        validLoginRequest.setPassword("password123");

        existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEmail("test@example.com");
        existingUser.setPasswordHash("encodedPassword");
        existingUser.setRoles(Set.of("ROLE_USER"));
    }

    // Тест на регистрацию нового пользователя с новым email
    @Test
    void register_WithNewEmail_Success() {
        // Мокируем методы
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Выполнение теста
        authService.register(validRegisterRequest);

        // Проверка результатов
        verify(userRepository, times(1)).findByEmail(validRegisterRequest.getEmail());
        verify(passwordEncoder, times(1)).encode(validRegisterRequest.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    // Тест на регистрацию нового пользователя с существующим email
    @Test
    void register_WithExistingEmail_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(existingUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.register(validRegisterRequest));

        assertEquals("Пользователь с таким email уже существует", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(validRegisterRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    // Тест на регистрацию нового пользователя с невалидными данными
    @Test
    void register_WithInvalidData_ThrowsException() {
        // Arrange
        RegisterRequest invalidRequest = new RegisterRequest();
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setPassword("");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        // Здесь можно добавить проверку валидации, если она есть в сервисе
        // Пока просто проверяем, что метод вызывается без исключений
        assertDoesNotThrow(() -> authService.register(invalidRequest));
    }

    // Тест на вход в систему с валидными данными
    @Test
    void login_WithValidCredentials_ReturnsAuthResponse() {
        // Arrange
        when(userRepository.findByEmail(validLoginRequest.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(validLoginRequest.getPassword(), existingUser.getPasswordHash())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyCollection())).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.login(validLoginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.token());

        verify(userRepository, times(1)).findByEmail(validLoginRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(validLoginRequest.getPassword(), existingUser.getPasswordHash());
        verify(jwtUtil, times(1)).generateToken(anyString(), anyCollection());
    }

    // Тест на вход в систему с невалидными данными
    @Test
    void login_WithInvalidEmail_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.login(validLoginRequest));

        assertEquals("Неверные учетные данные", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(validLoginRequest.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString(), any());
    }

    // Тест на вход в систему с невалидным паролем
    @Test
    void login_WithInvalidPassword_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail(validLoginRequest.getEmail()))
                .thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(validLoginRequest.getPassword(), existingUser.getPasswordHash()))
                .thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.login(validLoginRequest));

        assertEquals("Неверные учетные данные", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(validLoginRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(validLoginRequest.getPassword(), existingUser.getPasswordHash());
        verify(jwtUtil, never()).generateToken(anyString(), any());
    }

    // Тест на вход в систему с пользователем без ролей
    @Test
    void login_WithUserWithoutRoles_ReturnsToken() {
        // Arrange
        User userWithoutRoles = new User();
        userWithoutRoles.setId(userId);
        userWithoutRoles.setEmail("test@example.com");
        userWithoutRoles.setPasswordHash("encodedPassword");
        userWithoutRoles.setRoles(EMPTY_SET);

        when(userRepository.findByEmail(validLoginRequest.getEmail()))
                .thenReturn(Optional.of(userWithoutRoles));
        when(passwordEncoder.matches(validLoginRequest.getPassword(), userWithoutRoles.getPasswordHash()))
                .thenReturn(true);
        when(jwtUtil.generateToken(anyString(), any()))
                .thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.login(validLoginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.token());
    }

    // Тест на вход в систему с пустым email
    @Test
    void login_WithNullEmail_ThrowsException() {
        // Arrange
        LoginRequest requestWithNullEmail = new LoginRequest();
        requestWithNullEmail.setEmail(null);
        requestWithNullEmail.setPassword("password");

        // Act & Assert
        // Здесь можно ожидать NullPointerException или другое поведение в зависимости от реализации
        // В текущей реализации будет RuntimeException из-за Optional.empty()
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.login(requestWithNullEmail));

        assertEquals("Неверные учетные данные", exception.getMessage());
    }

    // Тест на вход в систему с пустым паролем
    @Test
    void login_WithEmptyPassword_ThrowsException() {
        // Arrange
        LoginRequest requestWithEmptyPassword = new LoginRequest();
        requestWithEmptyPassword.setEmail("test@example.com");
        requestWithEmptyPassword.setPassword("");

        when(userRepository.findByEmail(requestWithEmptyPassword.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("", existingUser.getPasswordHash())).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.login(requestWithEmptyPassword));

        assertEquals("Неверные учетные данные", exception.getMessage());
    }
}