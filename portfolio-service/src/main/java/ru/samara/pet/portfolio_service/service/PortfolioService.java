package ru.samara.pet.portfolio_service.service;

import lombok.AllArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.samara.pet.portfolio_service.model.Account;
import ru.samara.pet.portfolio_service.model.Transaction;
import ru.samara.pet.portfolio_service.repository.AccountRepository;
import ru.samara.pet.portfolio_service.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PortfolioService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public Account getUserAccount() {
        UUID currentUserId = getCurrentUserId();
        return accountRepository.findByUserId(currentUserId).get();
    }

    @Retryable(
            value = {org.springframework.dao.OptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 50)
    )
    @Transactional
    public void deposit(UUID accountId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        UUID currentUserId = getCurrentUserId();
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (!account.getUserId().equals(currentUserId)) {
            throw new SecurityException("Access denied");
        }

        // 1. Создаём транзакцию в статусе PENDING
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setAccountId(accountId);
        transaction.setAmount(amount);
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        transaction.setCreatedAt(Instant.now());
        transactionRepository.save(transaction);

        // 2. Обновляем баланс с оптимистичной блокировкой
        // JPA автоматически проверит @Version при вызове save()
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account); // может выбросить OptimisticLockException - ретрай повторит


        // 3. Помечаем транзакцию как завершённую
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setCompletedAt(Instant.now());
        transactionRepository.save(transaction);
    }

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }
        // Предполагается, что principal — это UUID строки или сам UUID
        Object principal = auth.getPrincipal();
        if (principal instanceof String) {
            return UUID.fromString((String) principal);
        } else if (principal instanceof UUID) {
            return (UUID) principal;
        } else {
            throw new IllegalStateException("Unsupported principal type: " + principal.getClass());
        }
    }
}
