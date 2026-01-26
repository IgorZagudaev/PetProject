package ru.samara.pet.auth_service.model;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.Type;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import ru.samara.pet.auth_service.model.dto.UserRegistered;

import java.time.LocalDateTime;
import java.util.UUID;

/*
CREATE TABLE outbox (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL, -- например, "User"
    aggregate_id UUID NOT NULL,           -- ID пользователя
    event_type VARCHAR(255) NOT NULL,     -- например, "AccountCreate"
    body JSONB,                           -- сериализованные данные
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    processed BOOLEAN DEFAULT false
);
 */
@Entity
@Table(name = "outbox")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Outbox {

    @Id
    @Column(columnDefinition = "id")
    UUID id;
    @Column(name = "aggregate_type")
    String aggregateType;
    @Column(name = "aggregate_id")
    UUID aggregateId;
    @Column(name = "event_type")
    String eventType;

    @Type(JsonBinaryType.class)
    @Column(name = "body",columnDefinition = "jsonb")
    UserRegistered body;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "processed")
    private boolean processed;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
