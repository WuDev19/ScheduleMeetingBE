package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.entity.RefreshToken;
import com.example.schedulemeetingbe.entity.Role;
import com.example.schedulemeetingbe.entity.User;
import com.example.schedulemeetingbe.exception.BusinessException;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.repository.RefreshTokenRepository;
import com.example.schedulemeetingbe.service.base.IJwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Component
@RequiredArgsConstructor
public class JwtServiceImpl implements IJwtService {

    @Value("${JWT_SECRET_KEY}")
    private String JWT_SECRET_KEY;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public String generateToken(String username, Long userId, Set<Role> roles) {
        var expiration = 3 * 24 * 60 * 60 * 1000;
        List<String> rolesUser = new ArrayList<>();
        List<String> permissions = new ArrayList<>();
        roles.forEach(role -> {
            rolesUser.add(role.getRoleName());
            role.getPermissions().forEach(permission ->
                    permissions.add(permission.getPermissionCode())
            );
        });
        return Jwts.builder()
                .subject(username)
                .claim("roles", rolesUser)
                .claim("permissions", permissions)
                .claim(StringCommon.USER_ID, userId)
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(Keys.hmacShaKeyFor(JWT_SECRET_KEY.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();
    }

    @Override
    public RefreshToken generateRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        return RefreshToken.builder()
                .userRefreshToken(user)
                .refreshToken(token)
                .expireDate(LocalDateTime.now().plusDays(7))
                .build();
    }

    @Override
    public RefreshToken verifyToken(RefreshToken refreshToken) {
        if (refreshToken.getIsRevoked()) {
            refreshTokenRepository.deleteByUserRefreshToken(refreshToken.getUserRefreshToken());
            throw new BusinessException(ErrorResponse.REFRESH_TOKEN_REVOKED);
        }
        if (refreshToken.getExpireDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorResponse.JWT_EXCEPTION);
        }
        return refreshToken;
    }

    @Override
    public RefreshToken rotateRefreshToken(RefreshToken refreshToken) {
        refreshToken.setIsRevoked(true);
        refreshTokenRepository.save(refreshToken);
        return generateRefreshToken(refreshToken.getUserRefreshToken());
    }

    @Override
    public Claims extractJwtClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(JWT_SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public String extractJwtId(String token) {
        return extractJwtClaims(token).getId();
    }

    @Override
    public LocalDateTime extractJwtExpire(String token) {
        return extractJwtClaims(token)
                .getExpiration()
                .toInstant()
                .atZone(ZoneId.of(StringCommon.TIME_ZONE_VN))
                .toLocalDateTime();
    }
}
