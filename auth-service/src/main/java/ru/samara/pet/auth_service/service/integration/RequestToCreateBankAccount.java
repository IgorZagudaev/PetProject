package ru.samara.pet.auth_service.service.integration;

import java.util.UUID;

public interface RequestToCreateBankAccount {

    public void sendRequestToCreateBankAccount (String jsonBody);
}
