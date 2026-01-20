package ru.samara.pet.auth_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Builder
@ToString(onlyExplicitlyIncluded = true) // Только с @Include
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Только с @Include
public class User {

    @Id
    @Column(columnDefinition = "uuid")
    @EqualsAndHashCode.Include // Только id участвует в equals/hashCode
    private UUID id = UUID.randomUUID();

    @NotBlank
    @Email
    @ToString.Include
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
    private String passwordHash;

    @Column(name = "first_name", length = 100)
    @ToString.Include
    private String firstName;

    @Column(name = "last_name", length = 100)
    @ToString.Include
    private String lastName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Enumerated(EnumType.STRING) // Храним как VARCHAR в БД
    @Column(name = "status", length = 20)
    private UserStatus status;

    @Column(name = "email_verified")
    @ToString.Include
    private Boolean emailVerified;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @ToString.Include
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @ToString.Include
    private LocalDateTime updatedAt;

    @Version // Оптимистичная блокировка
    @Column(name = "version")
    private Integer version;


    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>(Set.of("USER"));

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Enum для статусов
    public enum UserStatus {
        ACTIVE,
        BLOCKED,
        DELETED
    }

    // Методы для бизнес-логики
    public boolean isActive() {
        return UserStatus.ACTIVE.equals(this.status);
    }

    public boolean isBlocked() {
        return UserStatus.BLOCKED.equals(this.status);
    }

    public boolean isDeleted() {
        return UserStatus.DELETED.equals(this.status);
    }

    // Билдер для удобства создания
    public static UserBuilder builder(String email, String passwordHash) {
        return new UserBuilder()
                .email(email)
                .passwordHash(passwordHash)
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .version(0);
    }

}
