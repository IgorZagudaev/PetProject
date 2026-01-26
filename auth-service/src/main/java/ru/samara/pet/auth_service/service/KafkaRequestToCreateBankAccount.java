package ru.samara.pet.auth_service.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class KafkaRequestToCreateBankAccount implements RequestToCreateBankAccount{
    @Override
    public void sendRequestToCreateBankAccount(UUID userUUid) {

    }
}
