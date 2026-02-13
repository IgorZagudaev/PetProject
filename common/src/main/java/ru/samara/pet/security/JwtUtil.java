package ru.samara.pet.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

/**
 * библиотека JwtUtil
 *
 *
 */
public class JwtUtil {

    private final Key key;
    private final long expirationMs;

    public JwtUtil(String base64Secret, long expirationMs) {
        byte[] keyBytes = Decoders.BASE64.decode(base64Secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    /**
     * Generates a JWT token with the specified subject and authorities.
     * The token includes the subject, roles extracted from the authorities, issue date, and expiration date.
     * The token is signed using the HS256 algorithm with the provided key.
     *
     * @param subject     the subject for the token, typically a user identifier
     * @param authorities the collection of granted authorities to include as roles in the token
     * @return the generated JWT token as a string
     */
    public String generateToken(String subject, Collection<? extends GrantedAuthority> authorities) {
        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(subject)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates the specified JWT token.
     *
     * @param token the JWT token to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            System.err.println(e);
            return false;
        }
    }

    /**
     * Extracts the claims from the specified JWT token.
     *
     * @param token the JWT token to extract claims from
     * @return the claims extracted from the token
     */
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extracts the subject from the specified JWT token.
     *
     * @param token the JWT token to extract the subject from
     * @return the subject extracted from the token
     */
    public String extractSubject(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Extracts the authorities from the specified JWT token.
     *
     * @param token the JWT token to extract authorities from
     * @return the authorities extracted from the token
     */
    public Collection<? extends GrantedAuthority> extractAuthorities(String token) {
        Claims claims = extractClaims(token);
        List<String> roles = claims.get("roles", List.class);
        if (roles == null) {
            roles = Collections.emptyList();
        }
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
