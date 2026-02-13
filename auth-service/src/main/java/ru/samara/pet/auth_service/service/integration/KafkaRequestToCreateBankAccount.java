package ru.samara.pet.auth_service.service.integration;

import org.springframework.stereotype.Service;

@Service
public class KafkaRequestToCreateBankAccount implements RequestToCreateBankAccount{
    @Override
    public void sendRequestToCreateBankAccount(String jsonBody) {

    }
}
