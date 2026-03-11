package ru.samara.pet.auth_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic eventsTopic() {
        return TopicBuilder.name("events-topic")
                .partitions(3)
                .replicas(1) // для dev-среды достаточно 1
                .build();
    }
}
