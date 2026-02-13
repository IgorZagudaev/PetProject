package ru.samara.pet.auth_service.service.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.samara.pet.auth_service.model.Outbox;
import ru.samara.pet.auth_service.repository.OutboxRepository;
import ru.samara.pet.auth_service.service.integration.FeignRequestToCreateBankAccount;

import java.util.List;

@Service
@AllArgsConstructor
public class OutboxProcessRegisteredUser implements OutboxProcessEvent {

    private final FeignRequestToCreateBankAccount feignRequestToCreateBankAccount;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    @Scheduled(fixedDelay = 5000, initialDelay = 0)
    @Override
    public void process() {
        List<Outbox> outboxListToProcess = outboxRepository.find100ToProcess();
        outboxListToProcess.forEach(outbox -> {
            try {
                String payloadJson = objectMapper.writeValueAsString(outbox.getBody());
                feignRequestToCreateBankAccount.sendRequestToCreateBankAccount(payloadJson);
                outbox.setProcessed(true);
            } catch (Exception e) {
                System.out.println("Error processing outbox event: " + e.getMessage());
            }
        });

        if (!outboxListToProcess.isEmpty()) {
            outboxRepository.saveAll(outboxListToProcess); // обновляем статус
        }

    }
}
