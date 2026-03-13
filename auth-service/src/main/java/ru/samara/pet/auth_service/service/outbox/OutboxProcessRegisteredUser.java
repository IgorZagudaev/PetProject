package ru.samara.pet.auth_service.service.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.samara.pet.auth_service.model.Outbox;
import ru.samara.pet.auth_service.model.dto.CreateAccountCommand;
import ru.samara.pet.auth_service.repository.OutboxRepository;
import ru.samara.pet.auth_service.service.integration.FeignRequestToCreateBankAccount;
import ru.samara.pet.auth_service.service.integration.KafkaProducerService;
import ru.samara.pet.auth_service.service.integration.KafkaRequestToCreateBankAccount;
import ru.samara.pet.auth_service.service.integration.RequestToCreateBankAccount;

import java.util.List;

@Service
@AllArgsConstructor
public class OutboxProcessRegisteredUser implements OutboxProcessEvent {

    private final RequestToCreateBankAccount feignRequestToCreateBankAccount;
    private final OutboxRepository outboxRepository;
    private final KafkaRequestToCreateBankAccount kafkaRequestToCreateBankAccount;

    @Transactional
    @Async("ioExecutor")
    @Scheduled(fixedDelay = 5000, initialDelay = 0)
    @Override
    public void process() {
        List<Outbox> outboxListToProcess = outboxRepository.find100ToProcess();
        outboxListToProcess.forEach(outbox -> {
            try {
                outbox.setRetryCount((short)(outbox.getRetryCount()+ 1));
                outbox.setProcessed(true);
                CreateAccountCommand createAccountCommand = new CreateAccountCommand(outbox.getId());
                if (!feignRequestToCreateBankAccount.sendRequestToCreateBankAccount(createAccountCommand)) {
                    if (!kafkaRequestToCreateBankAccount.sendRequestToCreateBankAccount(createAccountCommand)) {
                        outbox.setProcessed(false);
                    }
                }
            } catch (Exception e) {
                outbox.setProcessed(false);
                System.out.println("Error processing outbox event: " + e.getMessage());
            }
        });

        if (!outboxListToProcess.isEmpty()) {
            outboxRepository.saveAll(outboxListToProcess); // обновляем статус
        }

    }
}
