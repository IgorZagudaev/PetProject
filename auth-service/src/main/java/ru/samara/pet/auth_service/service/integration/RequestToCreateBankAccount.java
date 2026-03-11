package ru.samara.pet.auth_service.service.integration;

import ru.samara.pet.auth_service.model.dto.CreateAccountCommand;

public interface RequestToCreateBankAccount {

    void sendRequestToCreateBankAccount (CreateAccountCommand createAccountCommand);
}
