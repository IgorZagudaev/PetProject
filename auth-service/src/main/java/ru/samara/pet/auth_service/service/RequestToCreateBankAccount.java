package ru.samara.pet.auth_service.service;

import java.util.UUID;

public interface RequestToCreateBankAccount {

    public void sendRequestToCreateBankAccount (UUID userUUid);
}
