package ru.samara.pet.auth_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.samara.pet.security.JwtUtil;

import java.io.IOException;
import java.util.Collection;

/**
 * Библиотека JwtAuthFilter для проверки токена.
 * .
 * Этот фильтр требует UserDetailsService. В микросервисах часто нет БД пользователей, кроме auth-service. Поэтому:
 * В auth-service — можно использовать настоящий UserDetailsService.
 * В других сервисах — можно создать упрощённый UserDetailsService, который просто проверяет email и роли из токена.
 *
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.extractSubject(token);
                Collection<? extends GrantedAuthority> authorities = jwtUtil.extractAuthorities(token);

                UserDetails userDetails = org.springframework.security.core.userdetails.User
                        .withUsername(email)
                        .authorities(authorities)
                        .password("") // не используется
                        .build();

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }
}