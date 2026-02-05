package ru.samara.pet.portfolio_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import ru.samara.pet.portfolio_service.model.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDTO {
    private UUID id;
    private UUID accountId;
    private BigDecimal amount;
    private Transaction.TransactionType type;
    private Transaction.TransactionStatus status;
    private Instant createdAt;
    private Instant completedAt;

    public static TransactionDTO fromEntity(Transaction transaction) {
        return TransactionDTO.builder()
                .id(transaction.getId())
                .accountId(transaction.getAccount() != null ? transaction.getAccount().getId() : null)
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .status(transaction.getStatus())
                .createdAt(transaction.getCreatedAt())
                .completedAt(transaction.getCompletedAt())
                .build();
    }
}
