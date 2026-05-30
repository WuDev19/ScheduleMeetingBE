package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.constant.enums.EVENT_TYPE;
import com.example.schedulemeetingbe.constant.enums.OutboxStatus;
import com.example.schedulemeetingbe.dto.common.CRUDResponseHelper;
import com.example.schedulemeetingbe.dto.request.LoginByUsernameRequest;
import com.example.schedulemeetingbe.dto.request.LogoutRequest;
import com.example.schedulemeetingbe.dto.request.SignUpWithUsernameRequest;
import com.example.schedulemeetingbe.dto.response.LoginResponse;
import com.example.schedulemeetingbe.entity.*;
import com.example.schedulemeetingbe.entity.payload.UserRegisteredPayload;
import com.example.schedulemeetingbe.exception.BusinessException;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.repository.*;
import com.example.schedulemeetingbe.service.base.IAuthenticationService;
import com.example.schedulemeetingbe.service.base.IJwtService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthenticationServiceImpl implements IAuthenticationService {
    private final BlackListAccessTokenRepository blackListAccessTokenRepository;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final IJwtService iJwtService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JsonMapper jsonMapper;

    @Override
    public boolean checkAccessTokenInBlacklist(String tokenId) {
        return blackListAccessTokenRepository.existsByTokenId(tokenId);
    }

    @Override
    public boolean checkUserExist(Long userId) {
        return userRepository.existsById(userId);
    }

    @Transactional
    @Override
    public LoginResponse login(LoginByUsernameRequest loginRequest) {
        String username = loginRequest.username();
        String password = loginRequest.password();
        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        boolean isMatch = bCryptPasswordEncoder.matches(password, user.getPasswordHash());
        if (!isMatch) {
            throw new BusinessException(ErrorResponse.PASSWORD_NOT_TRUE);
        }
        user.setLastLoginAt(ZonedDateTime.now());
        String token = iJwtService.generateToken(username, user.getUserId(), user.getRoles());
        RefreshToken refreshToken = iJwtService.generateRefreshToken(user);
        refreshTokenRepository.save(refreshToken);
        userRepository.save(user);
        return new LoginResponse(token, refreshToken.getRefreshToken());
    }

    @Transactional
    @Override
    public Map<String, Object> signUpWithUsername(SignUpWithUsernameRequest request) {
        String password = request.password();
        String passwordConfirm = request.passwordConfirm();
        if (!password.equals(passwordConfirm)) {
            throw new BusinessException(ErrorResponse.PASSWORD_NOT_MATCH);
        }
        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(bCryptPasswordEncoder.encode(password))
                .fullName(request.fullName())
                .phone(request.phone())
                .build();
        User userSaved = userRepository.save(user);

        //tạo verify-token phục vụ cho việc check khi gửi mail
        VerificationToken verificationToken = VerificationToken.builder()
                .token(UUID.randomUUID().toString())
                .user(userSaved)
                .expiresAt(ZonedDateTime.now().plusHours(1))
                .build();
        verificationTokenRepository.save(verificationToken);

        // tạo payload phục vụ cho lưu dạng jsonb trong postgres
        UserRegisteredPayload payload = new UserRegisteredPayload(
                userSaved.getUserId(),
                userSaved.getEmail(),
                verificationToken.getToken()
        );
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .eventType(EVENT_TYPE.USER_REGISTER.name())
                .payload(jsonMapper.valueToTree(payload))
                .status(OutboxStatus.PENDING)
                .build();
        outboxEventRepository.save(outboxEvent);
        return CRUDResponseHelper.createSuccess();
    }

    @Transactional
    @Override
    public LoginResponse refreshToken(String refToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByRefreshToken(refToken)
                .orElseThrow(() -> new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        RefreshToken validRefreshToken = iJwtService.verifyToken(refreshToken);
        RefreshToken newRefreshToken = iJwtService.rotateRefreshToken(validRefreshToken);
        RefreshToken saved = refreshTokenRepository.save(newRefreshToken);
        User user = saved.getUserRefreshToken();
        String token = iJwtService.generateToken(user.getUsername(), user.getUserId(), user.getRoles());
        return new LoginResponse(
                token,
                saved.getRefreshToken()
        );
    }

    @Transactional
    @Override
    public Map<String, Object> logout(LogoutRequest logoutRequest) {
        RefreshToken refreshToken = refreshTokenRepository.findByRefreshToken(logoutRequest.refreshToken()).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        refreshToken.setIsRevoked(true);
        String jwtId = iJwtService.extractJwtId(logoutRequest.accessToken());
        LocalDateTime expire = iJwtService.extractJwtExpire(logoutRequest.accessToken());
        BlackListAccessToken blacklistAccessToken = BlackListAccessToken.builder()
                .tokenId(jwtId)
                .expireDate(expire)
                .build();
        refreshTokenRepository.save(refreshToken);
        blackListAccessTokenRepository.save(blacklistAccessToken);
        return CRUDResponseHelper.modifySuccess();
    }

    @Transactional
    @Override
    public void verifyEmail(String token) {
        VerificationToken verification =
                verificationTokenRepository.findByToken(token)
                        .orElseThrow(() -> new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        if (verification.getVerified()) {
            return;
        }
        if (verification.getRevoked()) {
            throw new BusinessException(ErrorResponse.VERIFY_TOKEN_REVOKED);
        }
        if (verification.getExpiresAt()
                .isBefore(ZonedDateTime.now())) {
            verification.setRevoked(true);
            throw new BusinessException(ErrorResponse.VERIFY_TOKEN_EXPIRED);
        }
        verification.setVerified(true);
        verification.setRevoked(true);
        User user = verification.getUser();
        user.setIsActive(true);
        verificationTokenRepository.revokeAllVerificationTokenOfUser(user.getUserId(), verification.getId());
    }
}
