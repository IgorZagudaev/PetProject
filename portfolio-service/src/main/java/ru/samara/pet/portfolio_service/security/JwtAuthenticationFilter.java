package ru.samara.pet.portfolio_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.samara.pet.security.JwtUtil;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        System.out.println(">>> [JWT FILTER] Processing request: " + request.getRequestURI());
        System.out.println(">>> [JWT FILTER] Auth header: " + request.getHeader("Authorization"));

        String header = request.getHeader("Authorization");

        // Если заголовок содержит Bearer token, то проверяем его
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7); // "Bearer ".length()

            // Проверяем токен
            if (jwtUtil.validateToken(token)) {
                // Извлекаем userId из токена
                //String userId = jwtUtil.extractSubject(token)
                UUID userId = UUID.fromString(jwtUtil.extractSubject(token));

                // Создаём Authentication объект
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userId, // principal = UUID
                                null,
                                List.of() // authorities (можно пусто)
                        );

                // Устанавливаем в SecurityContext
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        // Передаем управление следующему фильтру
        filterChain.doFilter(request, response);
    }
}
