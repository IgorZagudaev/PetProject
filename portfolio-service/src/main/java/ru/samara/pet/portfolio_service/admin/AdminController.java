package ru.samara.pet.portfolio_service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheMonitoringService cacheMonitoringService;

    @DeleteMapping("/cache/clear")
    public String clearCache() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().flushDb();
        return "Кеш очищен!";
    }

    @DeleteMapping("/cache/clear/some")
    public String clearSomeCache() {
        return cacheMonitoringService.clearSomeCaches();
    }

    @GetMapping("/cache/stat")
    public String getCacheStat() {
        StringBuilder result = new StringBuilder();
        cacheMonitoringService.getCacheStats().forEach((k,v) -> result.append(k).append(" - ").append(v.toString()).append("\n"));
        return result.toString();
    }
}
