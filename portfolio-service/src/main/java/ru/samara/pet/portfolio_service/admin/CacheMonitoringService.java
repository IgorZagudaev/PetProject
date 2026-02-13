package ru.samara.pet.portfolio_service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheMonitoringService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Получить статистику кеша
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();

        Set<String> keys = redisTemplate.keys("accounts::*");
        stats.put("accountsCacheSize", keys.size());

        keys = redisTemplate.keys("account::*");
        stats.put("accountCacheSize", keys.size());

        // TTL для конкретного ключа
        Long ttl = redisTemplate.getExpire("accounts::all", TimeUnit.SECONDS);
        stats.put("accountsCacheTTL", ttl);

        return stats;
    }

    /**
     * Очистить кеш вручную (для админки)
     */
    @CacheEvict(value = {"accounts", "account", "transactions"}, allEntries = true)
    public String clearSomeCaches() {
        log.warn("Очистка всех кешей");
        return "Кеш очищен! для accounts, account, transactions";
    }
}