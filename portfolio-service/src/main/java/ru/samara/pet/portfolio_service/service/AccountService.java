package ru.samara.pet.portfolio_service.service;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.samara.pet.portfolio_service.exception.BusinessException;
import ru.samara.pet.portfolio_service.log.annotation.LogExecutionTime;
import ru.samara.pet.portfolio_service.mapper.AccountMapper;
import ru.samara.pet.portfolio_service.model.Account;
import ru.samara.pet.portfolio_service.model.dto.AccountDTO;
import ru.samara.pet.portfolio_service.model.dto.AccountResponse;
import ru.samara.pet.portfolio_service.model.dto.CreateAccountCommand;
import ru.samara.pet.portfolio_service.repository.AccountRepository;
import ru.samara.pet.portfolio_service.repository.TransactionRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final RedisTemplate<String, Object> redisTemplate;


    @PostConstruct
    public void init() {
        log.debug("=== RedisTemplate info ===");
        log.debug("RedisTemplate class: {}", redisTemplate.getClass().getName());
        log.debug("Value serializer: {}", redisTemplate.getValueSerializer().getClass().getName());
        log.debug("Key serializer: {}", redisTemplate.getKeySerializer().getClass().getName());
        log.debug("==========================");
    }

    public Account getUserAccount() {
        UUID currentUserId = getCurrentUserId();
        return accountRepository.findByUserId(currentUserId).get();
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

    @Cacheable(value = "accounts", key = "'all'", unless = "#result.isEmpty()")
    @Transactional(readOnly = true)
    @LogExecutionTime
    public List<AccountDTO> getAccounts() {
        List<Account> accounts =  accountRepository.getAllAccounts();
        return accounts.stream()
                .map(AccountDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    @LogExecutionTime
    @CacheEvict(value = "accounts", key = "'all'")
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

    /**
     * Удаление аккаунта — инвалидируем кеш
     */
    @CacheEvict(value = {"account", "accounts"}, allEntries = true)
    @Transactional
    @LogExecutionTime
    public void deleteAccount(UUID accountId) {
        log.info("Удаление аккаунта {}", accountId);
        accountRepository.deleteById(accountId);
    }




}
