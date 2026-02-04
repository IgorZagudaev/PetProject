package ru.samara.pet.portfolio_service.model.dto;

import ru.samara.pet.portfolio_service.model.Transaction;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountBalanceUpdateRequest(
                                          BigDecimal amount,
                                          Transaction.TransactionType transactionType) {
}
