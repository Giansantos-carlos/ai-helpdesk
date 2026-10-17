package com.giansantos.helpdesk.user.internal.domain;

import com.giansantos.helpdesk.user.api.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private Instant createdAt;

    public static User create(String email, String displayName, UserRole role) {
        User user = new User();
        user.id = UUID.randomUUID();
        user.email = email;
        user.displayName = displayName;
        user.role = role;
        user.createdAt = Instant.now();
        return user;
    }
}
