package ru.samara.pet.portfolio_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@Setter
public class Account {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, precision = 38, scale = 10)
    private BigDecimal balance = BigDecimal.ZERO;

    @Version
    private Long version; // для оптимистичной блокировки


}