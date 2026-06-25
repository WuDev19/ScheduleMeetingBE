package com.example.schedulemeetingbe.entity;

import com.example.schedulemeetingbe.utils.TimeUtils;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "verification_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(access = AccessLevel.PUBLIC)
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "verification_token_id",
            nullable = false,
            updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id",
            nullable = false)
    private User user;

    @Column(name = "token",
            nullable = false,
            unique = true,
            length = 255)
    private String token;

    @Column(name = "verified",
            nullable = false)
    @Builder.Default
    private Boolean verified = false;

    @Column(name = "revoked",
            nullable = false)
    @Builder.Default
    private Boolean revoked = false;

    @Column(name = "expires_at",
            nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "created_at",
            nullable = false,
            updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = TimeUtils.now();
    }
}