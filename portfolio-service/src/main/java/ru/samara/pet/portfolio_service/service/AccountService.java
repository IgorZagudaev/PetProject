package ru.samara.pet.portfolio_service.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.samara.pet.portfolio_service.model.Account;
import ru.samara.pet.portfolio_service.model.dto.CreateAccountCommand;
import ru.samara.pet.portfolio_service.repository.AccountRepository;

@Service
@AllArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public void createAccount(CreateAccountCommand command) {
        // Идемпотентность: не создаём аккаунт дважды
        if (accountRepository.findByUserId(command.userId()).isPresent()) {
            return;
        }
        Account account = new Account(command.userId());
        accountRepository.save(account);
    }
}
