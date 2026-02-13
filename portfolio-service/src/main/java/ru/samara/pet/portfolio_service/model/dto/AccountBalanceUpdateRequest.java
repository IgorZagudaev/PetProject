package ru.samara.pet.portfolio_service.model.dto;

import ru.samara.pet.portfolio_service.model.Transaction;

import java.math.BigDecimal;

public record AccountBalanceUpdateRequest(
                                          BigDecimal amount,
                                          Transaction.TransactionType transactionType) {
}
