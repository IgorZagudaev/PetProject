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
import ru.samara.pet.auth_service.service.integration.RequestToCreateBankAccount;

import java.util.List;

@Service
@AllArgsConstructor
public class OutboxProcessRegisteredUser implements OutboxProcessEvent {

    private final RequestToCreateBankAccount feignRequestToCreateBankAccount;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    @Async("ioExecutor")
    @Scheduled(fixedDelay = 5000, initialDelay = 0)
    @Override
    public void process() {
        List<Outbox> outboxListToProcess = outboxRepository.find100ToProcess();
        outboxListToProcess.forEach(outbox -> {
            try {
                CreateAccountCommand createAccountCommand = new CreateAccountCommand(outbox.getId());
                feignRequestToCreateBankAccount.sendRequestToCreateBankAccount(createAccountCommand);
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
