package ru.samara.pet.portfolio_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.samara.pet.portfolio_service.exception.JwtAuthenticationException;
import ru.samara.pet.security.JwtUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        System.out.println(">>> [JWT FILTER] Processing request: " + request.getRequestURI());
        System.out.println(">>> [JWT FILTER] Auth header: " + request.getHeader("Authorization"));
        try {
            String header = request.getHeader("Authorization");

            // Если заголовка нет - просто пропускаем запрос без аутентификации
            if (header == null || !header.startsWith("Bearer ")) {
                System.out.println(">>> [JWT FILTER] No valid Bearer token, proceeding without authentication");
                filterChain.doFilter(request, response);
                return;
            }

            String token = header.substring(7);


            // Проверяем токен
            if (!jwtUtil.validateToken(token)) {
                throw new JwtAuthenticationException("Невалидный JWT токен");
            }

            // Извлекаем userId из токена
            UUID userId = UUID.fromString(jwtUtil.extractSubject(token));

            // Создаём Authentication объект
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            userId, // principal = UUID
                            null,
                            List.of() // authorities (можно пусто)
                    );
            // Устанавливаем в SecurityContext
            System.out.println(">>> [JWT FILTER] Authentication created: " + auth);
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (JwtAuthenticationException ex) {
            System.out.println(">>> [JWT FILTER] Caught JwtAuthenticationException: ");
            // Обрабатываем ошибку аутентификации здесь
            handleAuthenticationException(response, ex, request.getRequestURI());
            return; // ВАЖНО: не передаем дальше по цепочке
        } catch (IllegalArgumentException e) {
            System.out.println(">>> [JWT FILTER] Caught IllegalArgumentException: ");
            handleAuthenticationException(response,new JwtAuthenticationException("Неверный формат userId в токене"), request.getRequestURI());
            return;
        }

        // Передаем управление следующему фильтру
        filterChain.doFilter(request, response);
    }

    private void handleAuthenticationException(HttpServletResponse response,
                                               AuthenticationException ex,
                                               String path) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        var errorResponse = ru.samara.pet.portfolio_service.exception.ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .errorCode("AUTH_FAILED")
                .message(ex.getMessage())
                .path(path)
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
