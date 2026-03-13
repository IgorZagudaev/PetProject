package ru.samara.pet.auth_service.service.integration;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.samara.pet.auth_service.model.dto.CreateAccountCommand;

@Service
@AllArgsConstructor
@Slf4j
public class FeignRequestToCreateBankAccount implements RequestToCreateBankAccount {

    private final NotificationFeignClient notificationClient;

    @Override
    public boolean sendRequestToCreateBankAccount(CreateAccountCommand createAccountCommand) {
        log.info("Send request to create bank account : {}", createAccountCommand.userId());

        // Отправляем напрямую как строку — без парсинга!
        try {
            ResponseEntity<String> response = notificationClient.sendEvent(createAccountCommand);
            // ответ от p-s
            log.info("Response body: {}", response.getBody());
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Request sent successfully");
            } else {
                log.info("Request failed with status code: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
        return true;

    }
}
