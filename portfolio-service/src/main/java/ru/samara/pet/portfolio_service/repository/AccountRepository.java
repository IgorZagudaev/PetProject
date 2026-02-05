package ru.samara.pet.portfolio_service.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.samara.pet.portfolio_service.model.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository  extends JpaRepository<Account, UUID> {
    Optional<Account> findByUserId(UUID userId);

    @Override
    Optional<Account> findById(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdWithLock(@Param("id") UUID id);

    @Query("SELECT DISTINCT a FROM Account a LEFT JOIN FETCH a.transactions")
    List<Account> getAllAccounts();
}
