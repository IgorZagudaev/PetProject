package ru.samara.pet.portfolio_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import ru.samara.pet.portfolio_service.model.Account;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDTO {
    private UUID id;
    private UUID userId;
    private BigDecimal balance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    // Статический метод для конвертации из сущности
    public static AccountDTO fromEntity(Account account) {
        return AccountDTO.builder()
                .id(account.getId())
                .userId(account.getUserId())
                .balance(account.getBalance())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .version(account.getVersion())
                .build();
    }
}
