package ru.samara.pet.portfolio_service.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.samara.pet.portfolio_service.exception.BusinessException;
import ru.samara.pet.portfolio_service.log.annotation.LogExecutionTime;
import ru.samara.pet.portfolio_service.mapper.AccountMapper;
import ru.samara.pet.portfolio_service.model.Account;
import ru.samara.pet.portfolio_service.model.Transaction;
import ru.samara.pet.portfolio_service.model.dto.AccountBalanceUpdateRequest;
import ru.samara.pet.portfolio_service.model.dto.AccountResponse;
import ru.samara.pet.portfolio_service.model.dto.CreateAccountCommand;
import ru.samara.pet.portfolio_service.repository.AccountRepository;
import ru.samara.pet.portfolio_service.repository.TransactionRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountMapper accountMapper;

    @Transactional
    @LogExecutionTime
    public AccountResponse createAccount(CreateAccountCommand command) {
        // Идемпотентность: не создаём аккаунт дважды
        if (accountRepository.findByUserId(command.userId()).isPresent()) {
            throw new BusinessException("Email уже существует");
        }
        Account account = new Account(command.userId());
        Account savedAccount = accountRepository.save(account);

        // Преобразование в ответ
        return accountMapper.toResponse(savedAccount);
    }

    @Transactional
    @LogExecutionTime
    public Account deposit(UUID accountId, AccountBalanceUpdateRequest request) {
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        Account account = accountRepository.findById(accountId)
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

    @Transactional
    @LogExecutionTime
    public Account withdraw(UUID accountId, AccountBalanceUpdateRequest request) {
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        Account account = accountRepository.findById(accountId)
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

}
