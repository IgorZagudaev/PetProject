package ru.samara.pet.auth_service.service.integration;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class FeignRequestToCreateBankAccount implements RequestToCreateBankAccount {

    private final NotificationFeignClient notificationClient;

    @Override
    public void sendRequestToCreateBankAccount(String jsonBody) {
        System.out.println("Send request to create bank account with body: " + jsonBody);

        // Отправляем напрямую как строку — без парсинга!
        ResponseEntity<String> response = notificationClient.sendEvent(jsonBody);

        // ответ от p-s
        System.out.println("Response body: " + response.getBody());

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Request sent successfully");
        } else {
            System.out.println("Request failed with status code: " + response.getStatusCode());
            // пробросить исключение выше
            throw new RuntimeException("Request failed with status code: " + response.getStatusCode());
        }

    }
}
