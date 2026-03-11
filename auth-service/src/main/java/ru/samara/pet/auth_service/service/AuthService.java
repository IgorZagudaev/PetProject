package ru.samara.pet.auth_service.service;


import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.samara.pet.auth_service.model.Outbox;
import ru.samara.pet.auth_service.model.dto.AuthResponse;
import ru.samara.pet.auth_service.model.dto.LoginRequest;
import ru.samara.pet.auth_service.model.dto.RegisterRequest;
import ru.samara.pet.auth_service.model.User;
import ru.samara.pet.auth_service.model.dto.UserRegistered;
import ru.samara.pet.auth_service.repository.OutboxRepository;
import ru.samara.pet.auth_service.repository.UserRepository;
import ru.samara.pet.security.JwtUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final String AGGREGATE_TYPE = "auth service";
    private static final String EVENT_TYPE_REGISTERED = "Registered user";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OutboxRepository outboxRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public void register(RegisterRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email не может быть пустым");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new RuntimeException("Пароль должен быть не менее 6 символов");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user = userRepository.save(user);

        // Transactional Outbox
        UserRegistered userRegistered = new UserRegistered(user.getId());
        Outbox outbox = new Outbox();
        outbox.setAggregateId(user.getId());
        outbox.setAggregateType(AGGREGATE_TYPE);
        outbox.setEventType(EVENT_TYPE_REGISTERED);
        outbox.setBody(userRegistered);
        outboxRepository.save(outbox);
        log.info("User registered: {}", user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email не может быть пустым");
        }
        if (request.getPassword() == null) {
            throw new RuntimeException("Пароль не может быть пустым");
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Неверные учетные данные"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Неверные учетные данные");
        }

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(user.getRoles().toArray(new String[0]))
                .build();

        //String token = jwtUtil.generateToken(userDetails.getUsername(), userDetails.getAuthorities());
        String token = jwtUtil.generateToken(user.getId().toString(), userDetails.getAuthorities());
        log.info("User logged in: {}", user);
        return new AuthResponse(token);
    }
}