package ru.samara.pet.auth_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
public class MixedAsyncConfig {

    // Для IO-bound задач
    @Bean(name = "ioExecutor")
    public Executor ioExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    // Для CPU-bound задач
    @Bean(name = "cpuExecutor")
    public Executor cpuExecutor() {
        int processors = Runtime.getRuntime().availableProcessors();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(processors);
        executor.setMaxPoolSize(processors);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("cpu-");
        executor.initialize();
        return executor;
    }
}
