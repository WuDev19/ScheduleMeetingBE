package com.example.schedulemeetingbe.entity;

import com.example.schedulemeetingbe.constant.StringCommon;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Entity
@Table(name = "refresh_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(access = AccessLevel.PUBLIC)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_token_id")
    private Long refreshId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User userRefreshToken;

    @Column(name = "refresh_token", unique = true, nullable = false, length = 500)
    private String refreshToken;

    @Column(name = "expire_date", nullable = false)
    private ZonedDateTime expireDate;

    @Builder.Default
    @Column(name = "is_revoked", nullable = false)
    private Boolean isRevoked = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @PrePersist
    public void onCreate(){
        this.createdAt = ZonedDateTime.now(ZoneOffset.UTC);
    }
}
