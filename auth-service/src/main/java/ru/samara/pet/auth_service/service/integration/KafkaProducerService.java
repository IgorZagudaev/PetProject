package ru.samara.pet.auth_service.service.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import ru.samara.pet.auth_service.model.dto.CreateAccountCommand;
import ru.samara.pet.auth_service.model.dto.RegisterRequest;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, CreateAccountCommand> kafkaTemplate;
    private static final String TOPIC_NAME = "events-topic";

    public void sendEventCreateAccount(CreateAccountCommand createAccountCommand) {
        kafkaTemplate.send(TOPIC_NAME, createAccountCommand.userId().toString(), createAccountCommand)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Message sent: offset={}, partition={}",
                                result.getRecordMetadata().offset(),
                                result.getRecordMetadata().partition());
                    } else {
                        log.error("Failed to send message", ex);
                    }
                });
    }

    // Опционально: отправка с колбэком и таймаутом
    public CompletableFuture<SendResult<String, CreateAccountCommand>> sendEventCreateAccountAsync(CreateAccountCommand createAccountCommand) {
        return kafkaTemplate.send(TOPIC_NAME, createAccountCommand.userId().toString(), createAccountCommand);
    }
}
