package ru.samara.pet.portfolio_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true) // Только с @Include
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Только с @Include
public class Account {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(nullable = false, unique = true)
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID userId;

    @Column(nullable = false, precision = 38, scale = 10)
    @ToString.Include
    private BigDecimal balance = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @ToString.Include
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @ToString.Include
    private LocalDateTime updatedAt;

    @Version
    private Long version; // для оптимистичной блокировки

    public Account(UUID userId) {
        this.userId = userId;
    }

    // Методы бизнес-логики
    public void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.balance = this.balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }
        this.balance = this.balance.subtract(amount);
    }


}