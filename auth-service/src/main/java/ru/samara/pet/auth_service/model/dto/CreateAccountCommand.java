package ru.samara.pet.auth_service.model.dto;

import java.util.UUID;

public record CreateAccountCommand(UUID userId) {
}
