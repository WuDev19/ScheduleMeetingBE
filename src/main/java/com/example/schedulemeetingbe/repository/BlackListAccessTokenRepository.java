package com.example.schedulemeetingbe.repository;

import com.example.schedulemeetingbe.entity.BlackListAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlackListAccessTokenRepository extends JpaRepository<BlackListAccessToken, Long> {
    boolean existsByTokenId(String token);

}
