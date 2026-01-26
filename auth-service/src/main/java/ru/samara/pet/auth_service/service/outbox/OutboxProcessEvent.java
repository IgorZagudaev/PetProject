package ru.samara.pet.auth_service.service.outbox;

public interface OutboxProcessEvent {
    public void process();
}
