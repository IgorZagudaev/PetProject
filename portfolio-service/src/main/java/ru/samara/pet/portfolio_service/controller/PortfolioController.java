package ru.samara.pet.portfolio_service.controller;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import ru.samara.pet.portfolio_service.exception.BusinessException;
import ru.samara.pet.portfolio_service.log.annotation.LogExecutionTime;
import ru.samara.pet.portfolio_service.mapper.AccountMapper;
import ru.samara.pet.portfolio_service.model.Account;
import ru.samara.pet.portfolio_service.model.Transaction;
import ru.samara.pet.portfolio_service.model.dto.AccountBalanceUpdateRequest;
import ru.samara.pet.portfolio_service.model.dto.AccountDTO;
import ru.samara.pet.portfolio_service.model.dto.AccountResponse;
import ru.samara.pet.portfolio_service.model.dto.CreateAccountCommand;
import ru.samara.pet.portfolio_service.service.AccountService;
import ru.samara.pet.portfolio_service.service.PortfolioService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
@Slf4j
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final AccountService accountService;
    private final AccountMapper accountMapper;

    @PostMapping("/accounts/create")
    public ResponseEntity<AccountResponse> createAccount(@RequestBody CreateAccountCommand createAccountCommand) {
        log.info("Creating account v1: {}", createAccountCommand.userId());

        try {
            AccountResponse accountResponse = accountService.createAccount(createAccountCommand);
            return ResponseEntity.status(HttpStatus.CREATED).body(accountResponse);
        } catch (Exception er) {
            log.error("createAccount - {}",String.valueOf(er));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }


    }

    /*
    PATCH /api/v1/accounts/550e8400-e29b-41d4-a716-446655440000/balance
    {
      "amount": 1000.00,
      "transactionType": "DEPOSIT"
    }
     */
    @PatchMapping("/accounts/{accountId}/balance")
    public ResponseEntity<AccountResponse> updateBalance(
            @PathVariable @NotNull UUID accountId,
            @Valid @RequestBody AccountBalanceUpdateRequest request) {

        log.info("update balance: {}", request);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            log.info("NO AUTHENTICATION!");
            throw new SecurityException("NO AUTHENTICATION!");
        }
        Account updatedAccount = null;

        if (Transaction.TransactionType.DEPOSIT.equals(request.transactionType())) {
            updatedAccount = portfolioService.deposit(accountId, request);
        }
        if (Transaction.TransactionType.WITHDRAWAL.equals(request.transactionType())) {
            updatedAccount = portfolioService.withdraw(accountId, request);
        }
        if (updatedAccount==null) {
            throw new BusinessException("No such transaction type");
        }

        AccountResponse response = accountMapper.toResponse(updatedAccount);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/current_accounts")
    public ResponseEntity<List<Account>> getUserAccounts() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("Authentication: {}", auth);
        if (auth == null) {
            System.out.println("NO AUTHENTICATION!");
        } else {
            log.info("Principal: {}", auth.getPrincipal());
            log.info("Authenticated: {}", auth.isAuthenticated());
        }
        return ResponseEntity.ok(List.of(accountService.getUserAccount()));
    }

    @GetMapping("/accounts")
    @LogExecutionTime
    public ResponseEntity<List<AccountDTO>> getAccounts() {
        //accountService.getAccounts().forEach(System.out::println);
        return ResponseEntity.ok(accountService.getAccounts());
    }




}
