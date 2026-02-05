package ru.samara.pet.portfolio_service.service;

import jakarta.persistence.OptimisticLockException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.samara.pet.portfolio_service.exception.BusinessException;
import ru.samara.pet.portfolio_service.log.annotation.LogExecutionTime;
import ru.samara.pet.portfolio_service.model.Account;
import ru.samara.pet.portfolio_service.model.Transaction;
import ru.samara.pet.portfolio_service.model.dto.AccountBalanceUpdateRequest;
import ru.samara.pet.portfolio_service.repository.AccountRepository;
import ru.samara.pet.portfolio_service.repository.TransactionRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class PortfolioService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Transactional(timeout = 5)
    @LogExecutionTime
    @Retryable(
            retryFor = {
                    PessimisticLockingFailureException.class,
                    CannotAcquireLockException.class,
                    OptimisticLockException.class
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 50, multiplier = 2),
            recover = "recoverDeposit"
    )
    public Account deposit(UUID accountId, AccountBalanceUpdateRequest request) {
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        Account account = accountRepository.findByIdWithLock(accountId)
                .orElseThrow(() -> new BusinessException("No such account"));
        account.setBalance(account.getBalance().add(request.amount()));

        // Создаём запись транзакции
        Transaction transaction = Transaction.builder()
                .account(account)
                .type(request.transactionType())
                .amount(request.amount())
                .status(Transaction.TransactionStatus.COMPLETED)
                .build();
        accountRepository.save(account);
        transactionRepository.save(transaction);
        return account;
    }

    @Transactional(timeout = 5)
    @LogExecutionTime
    @Retryable(
            retryFor = {
                    PessimisticLockingFailureException.class,
                    CannotAcquireLockException.class,
                    OptimisticLockException.class
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 50, multiplier = 2),
            recover = "recoverWithdraw"
    )
    public Account withdraw(UUID accountId, AccountBalanceUpdateRequest request) {
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        Account account = accountRepository.findByIdWithLock(accountId)
                .orElseThrow(() -> new BusinessException("No such account"));
        if (account.getBalance().compareTo(request.amount()) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }

        account.setBalance(account.getBalance().subtract(request.amount()));

        // Создаём запись транзакции
        Transaction transaction = Transaction.builder()
                .account(account)
                .type(request.transactionType())
                .amount(request.amount())
                .status(Transaction.TransactionStatus.COMPLETED)
                .build();

        accountRepository.save(account);
        transactionRepository.save(transaction);
        return account;
    }

    @Recover
    public void recoverWithdraw(Exception e, UUID accountId, BigDecimal amount) {
        log.error("Не удалось выполнить операцию снятия {} со счёта {} после 3 попыток",
                amount, accountId, e);
        throw new BusinessException("Не удалось выполнить операцию снятия");
    }

    @Recover
    public void recoverDeposit(Exception e, UUID accountId, BigDecimal amount) {
        log.error("Не удалось выполнить операцию пополнения {} со счёта {} после 3 попыток",
                amount, accountId, e);
        throw new BusinessException("Не удалось выполнить операцию пополнения");
    }


}
