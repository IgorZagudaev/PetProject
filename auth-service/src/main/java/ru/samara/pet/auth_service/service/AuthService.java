package ru.samara.pet.auth_service.service;


import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OutboxRepository outboxRepository;
    private final JwtUtil jwtUtil;
    //private final RequestToCreateBankAccount requestToCreateBankAccount;

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        // Transactional Outbox
        UserRegistered userRegistered = new UserRegistered(user.getId());
        Outbox outbox = new Outbox();
        outbox.setAggregateId(user.getId());
        outbox.setAggregateType("auth service");
        outbox.setEventType("Registered user");
        outbox.setBody(userRegistered);
        outboxRepository.save(outbox);


    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
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
        return new AuthResponse(token);
    }
}