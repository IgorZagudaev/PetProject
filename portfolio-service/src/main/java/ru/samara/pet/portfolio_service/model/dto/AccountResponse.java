package ru.samara.pet.portfolio_service.model.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AccountResponse(UUID id, UUID userId, LocalDateTime createdAt) {
}
