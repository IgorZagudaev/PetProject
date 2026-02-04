package ru.samara.pet.portfolio_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.samara.pet.portfolio_service.exception.ErrorResponse;
import ru.samara.pet.portfolio_service.security.JwtAuthenticationFilter;
import ru.samara.pet.security.JwtUtil;

import java.time.LocalDateTime;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Bean
    public JwtUtil jwtUtil(@Value("${app.jwt.secret}") String secret,
                           @Value("${app.jwt.expiration-ms}") long expirationMs) {
        return new JwtUtil(secret, expirationMs);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthFilter) throws Exception {
        http
                // включаем CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // отключаем сессии
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // авторизация
                .authorizeHttpRequests(authz -> authz
                        //http://localhost:8082/api/v1/accounts/create
                        .requestMatchers("/api/v1/accounts").authenticated()
                        .requestMatchers( "/api/v1/accounts/*/balance").authenticated()

                        .anyRequest().permitAll()
                )
                .httpBasic(AbstractHttpConfigurer::disable) // не используем Basic Auth
                .formLogin(AbstractHttpConfigurer::disable) // отключаем форму логина
                // фильтры
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


}
