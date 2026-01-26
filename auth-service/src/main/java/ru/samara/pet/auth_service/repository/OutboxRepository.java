package ru.samara.pet.auth_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.samara.pet.auth_service.model.Outbox;

import java.util.UUID;

public interface OutboxRepository extends JpaRepository<Outbox, UUID> {
}
