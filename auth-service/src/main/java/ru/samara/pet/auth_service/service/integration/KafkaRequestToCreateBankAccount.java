package ru.samara.pet.auth_service.service.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.samara.pet.auth_service.model.dto.CreateAccountCommand;
import ru.samara.pet.auth_service.model.dto.RegisterRequest;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaRequestToCreateBankAccount implements RequestToCreateBankAccount{

    private final KafkaProducerService kafkaProducerService;

    @Override
    public boolean sendRequestToCreateBankAccount(CreateAccountCommand createAccountCommand) {
        kafkaProducerService.sendEventCreateAccount(createAccountCommand);
        return true;
    }
}
