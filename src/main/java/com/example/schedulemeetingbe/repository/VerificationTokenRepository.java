package com.example.schedulemeetingbe.repository;

import com.example.schedulemeetingbe.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    Optional<VerificationToken> findByToken(String token);

    @Modifying
    @Query("""
            UPDATE VerificationToken vt
            SET vt.revoked = true
            WHERE vt.user.userId = :userId
            AND vt.revoked = false
            AND vt.id <> :currentTokenId
            """)
    void revokeAllVerificationTokenOfUser(@Param("userId") Long userId, @Param("currentTokenId") UUID currentTokenId);

}
