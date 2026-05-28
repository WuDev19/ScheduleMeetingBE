package com.example.schedulemeetingbe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "black_list_access_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(access = AccessLevel.PUBLIC)
public class BlackListAccessToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blacklist_token_id")
    private Long blacklistTokenId;

    @Column(name = "token_id", nullable = false, length = 36, unique = true)
    private String tokenId;

    // Lưu thời gian hết hạn gốc của JWT để sau này chạy ngầm
    // tự động xóa các token đã quá hạn ra khỏi DB cho nhẹ bảng.
    @Column(name = "expire_date", nullable = false)
    private LocalDateTime expireDate;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

