package ru.samara.pet.portfolio_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    @Column(name = "created_at", updatable = false)
    @ToString.Include
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @ToString.Include
    private LocalDateTime updatedAt;

    @Version
    private Long version; // для оптимистичной блокировки

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private List<Transaction> transactions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Account(UUID userId) {
        this.userId = userId;
    }

    // Методы бизнес-логики



}