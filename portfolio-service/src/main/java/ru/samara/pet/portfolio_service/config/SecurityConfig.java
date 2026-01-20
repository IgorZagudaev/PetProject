package ru.samara.pet.portfolio_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.samara.pet.portfolio_service.security.JwtAuthenticationFilter;
import ru.samara.pet.security.JwtUtil;

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
                .csrf(csrf -> csrf.disable())
                // отключаем сессии
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // авторизация
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/v1/accounts").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/accounts/*/deposit").authenticated()
                        .anyRequest().permitAll()
                )
                // фильтры
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
