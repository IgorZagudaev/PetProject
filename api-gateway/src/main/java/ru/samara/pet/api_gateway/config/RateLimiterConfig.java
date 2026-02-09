package ru.samara.pet.api_gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class RateLimiterConfig {


    @Bean
    KeyResolver keyResolver() {
        return exchange ->

                Mono.just("RequestLimiterKey");
    }

    // Лимит по пользователю и путь запроса
    @Bean
    public KeyResolver compositeKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");
            String path = exchange.getRequest().getPath().value();
            log.debug("RateLimiter(compositeKeyResolver) key for request: {}:{}", userId, path);
            return Mono.just(userId + ":" + path);
        };
    }

    // Лимит по пользователю (из заголовка)
    @Bean
    @Primary
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");
            String key = (userId != null) ? userId : "anonymous";
            log.debug("RateLimiter(userKeyResolver) key for request: {}", key);
            return Mono.just(key);
        };
    }

    // Лимит по IP-адресу
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest().getRemoteAddress().getHostName()
        );
    }

    // Лимит по API-ключу
    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> Mono.justOrEmpty(
                exchange.getRequest().getHeaders().getFirst("X-API-Key")
        );
    }
}
