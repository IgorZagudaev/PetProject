package ru.samara.pet.portfolio_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.samara.pet.portfolio_service.model.Transaction;

import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

}
