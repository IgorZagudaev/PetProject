package ru.samara.pet.auth_service.model.dto;

import java.io.Serializable;

public record AuthResponse(String token)  implements Serializable {
}
