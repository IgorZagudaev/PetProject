package ru.samara.pet.portfolio_service.controller;

import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.samara.pet.portfolio_service.model.Account;
import ru.samara.pet.portfolio_service.service.PortfolioService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping("/accounts")
    public ResponseEntity<List<Account>> getUserAccounts() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Authentication: " + auth);
        if (auth == null) {
            System.out.println("NO AUTHENTICATION!");
        } else {
            System.out.println("Principal: " + auth.getPrincipal());
            System.out.println("Authenticated: " + auth.isAuthenticated());
        }
        return ResponseEntity.ok(portfolioService.getUserAccounts());
    }

    @PostMapping("/accounts/{accountId}/deposit")
    public ResponseEntity<Void> deposit(@PathVariable UUID accountId,
                                        @RequestBody DepositRequest request) {
        portfolioService.deposit(accountId, request.getAmount());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/accounts/create")
    public ResponseEntity<Void> createAccount(@RequestBody String request) {
        System.out.println("Creating account: " + request);
        return ResponseEntity.ok().build();
    }

    public static class DepositRequest {
        private BigDecimal amount;

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }
}
