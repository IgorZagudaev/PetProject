package ru.samara.pet.auth_service.service.outbox;

public interface OutboxProcessEvent {
    void process();
}
