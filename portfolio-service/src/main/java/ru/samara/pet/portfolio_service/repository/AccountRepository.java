package ru.samara.pet.portfolio_service.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import ru.samara.pet.portfolio_service.model.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository  extends JpaRepository<Account, UUID> {
    Optional<Account> findByUserId(UUID userId);

    Optional<Account> findById(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE) // или можно без этого, если используем @Version + retry
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Account findWithLockById(UUID id);
}
