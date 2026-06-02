package com.example.schedulemeetingbe.service.base;

import com.example.schedulemeetingbe.entity.RefreshToken;
import com.example.schedulemeetingbe.entity.Role;
import com.example.schedulemeetingbe.entity.User;
import io.jsonwebtoken.Claims;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Set;


public interface IJwtService {
    String generateToken(String username, Long userId, Set<Role> roles);

    RefreshToken generateRefreshToken(User user);

    RefreshToken verifyToken(RefreshToken refreshToken);

    RefreshToken rotateRefreshToken(RefreshToken refreshToken);

    Claims extractJwtClaims(String token);

    String extractJwtId(String token);

    ZonedDateTime extractJwtExpire(String token);
}
