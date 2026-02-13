package ru.samara.pet.auth_service.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.samara.pet.auth_service.model.Outbox;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<Outbox, UUID> {
    @Query(value = "SELECT * FROM outbox WHERE processed=false LIMIT 100",
            nativeQuery = true)
    List<Outbox> find100ToProcess();
}
