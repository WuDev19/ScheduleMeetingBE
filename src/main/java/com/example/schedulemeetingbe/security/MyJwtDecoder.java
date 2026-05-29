package com.example.schedulemeetingbe.security;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.exception.BusinessException;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.service.base.IAuthenticationService;
import com.example.schedulemeetingbe.service.base.IJwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class MyJwtDecoder implements JwtDecoder {
    private final NimbusJwtDecoder nimbusJwtDecoder;
    private final IJwtService iJwtService;
    private final IAuthenticationService iAuthenticationService;

    public MyJwtDecoder(@Value("${JWT_SECRET_KEY}") String JWT_SECRET_KEY,
                        IAuthenticationService iAuthenticationService,
                        IJwtService iJwtService) {
        this.iAuthenticationService = iAuthenticationService;
        this.nimbusJwtDecoder = NimbusJwtDecoder
                .withSecretKey(Keys.hmacShaKeyFor(JWT_SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        this.iJwtService = iJwtService;
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        Jwt jwt = nimbusJwtDecoder.decode(token);
        String tokenId = jwt.getId();
        if (iAuthenticationService.checkAccessTokenInBlacklist(tokenId)) {
            throw new JwtException(StringCommon.TOKEN_IN_BLACKLIST);
        }
        Claims claims = iJwtService.extractJwtClaims(token);
        if (!iAuthenticationService.checkUserExist(claims.get(StringCommon.USER_ID, Long.class))) {
            throw new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND);
        }
        return jwt;
    }
}
