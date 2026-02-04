package ru.samara.pet.portfolio_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Transaction {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private UUID id;

    @EqualsAndHashCode.Include
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(nullable = false, precision = 38, scale = 10)
    @ToString.Include
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "oper_type")
    @ToString.Include
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ToString.Include
    private TransactionStatus status;

    @Column(nullable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column
    @ToString.Include
    private Instant completedAt;


    public enum TransactionStatus {
        PENDING, COMPLETED, FAILED
    }

    public enum TransactionType {
        DEPOSIT, WITHDRAWAL
    }


}
