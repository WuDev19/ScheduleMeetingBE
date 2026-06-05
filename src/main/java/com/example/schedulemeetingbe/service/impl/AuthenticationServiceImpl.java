package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.constant.enums.EVENT_TYPE;
import com.example.schedulemeetingbe.constant.enums.OutboxStatus;
import com.example.schedulemeetingbe.dto.common.CRUDResponseHelper;
import com.example.schedulemeetingbe.dto.request.LoginByUsernameRequest;
import com.example.schedulemeetingbe.dto.request.LogoutRequest;
import com.example.schedulemeetingbe.dto.request.SendEmailRequest;
import com.example.schedulemeetingbe.dto.request.SignUpWithUsernameRequest;
import com.example.schedulemeetingbe.dto.response.LoginResponse;
import com.example.schedulemeetingbe.entity.*;
import com.example.schedulemeetingbe.entity.payload.UserRegisteredPayload;
import com.example.schedulemeetingbe.entity.payload.UserResetPasswordPayload;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.exception.custom_exception.BusinessException;
import com.example.schedulemeetingbe.exception.custom_exception.CooldownResendException;
import com.example.schedulemeetingbe.repository.*;
import com.example.schedulemeetingbe.service.base.IAuthenticationService;
import com.example.schedulemeetingbe.service.base.IJwtService;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.example.schedulemeetingbe.constant.Constants.COOLDOWN_EMAIL_RESET_PASSWORD_TIME;
import static com.example.schedulemeetingbe.constant.Constants.COOLDOWN_RESEND_EMAIL_TIME;

@Service
@AllArgsConstructor
public class AuthenticationServiceImpl implements IAuthenticationService {
    private final BlackListAccessTokenRepository blackListAccessTokenRepository;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final RoleRepository roleRepository;
    private final IJwtService iJwtService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JsonMapper jsonMapper;
    private final RedisTemplate<String, Object> redisTemplate;

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
        user.setLastLoginAt(ZonedDateTime.now(ZoneOffset.UTC));
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
        Role role = roleRepository.findByRoleName(request.role()).orElseThrow(() -> new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(bCryptPasswordEncoder.encode(password))
                .fullName(request.fullName())
                .phone(request.phone())
                .roles(Set.of(role))
                .build();
        User userSaved = userRepository.save(user);

        //tạo verify-token phục vụ cho việc check khi gửi mail
        VerificationToken verificationToken = VerificationToken.builder()
                .token(UUID.randomUUID().toString())
                .user(userSaved)
                .expiresAt(ZonedDateTime.now(ZoneOffset.UTC).plusHours(1))
                .build();
        return createUserRegisterVerificationTokenAndOutboxEvent(userSaved, verificationToken, EVENT_TYPE.USER_REGISTER);
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
        ZonedDateTime expire = iJwtService.extractJwtExpire(logoutRequest.accessToken());
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
        User user = verification.getUser();
        if (verification.getVerified()) { //nếu bấm lại link xác nhận cũ
            return;
        }
        if (user.getIsActive()) { //nếu xác nhận bằng verification token khác
            verification.setRevoked(true);
            return;
        }
        checkVerifyToken(verification);
        user.setIsActive(true);
        verificationTokenRepository.revokeAllVerificationTokenOfUser(user.getUserId());
    }

    @Transactional
    @Override
    public void verifyUpdateEmail(String token, String newEmail) {
        VerificationToken verification =
                verificationTokenRepository.findByToken(token)
                        .orElseThrow(() -> new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        User user = verification.getUser();
        if (verification.getVerified()) {
            return;
        }
        checkVerifyToken(verification);
        user.setEmail(newEmail);
        verificationTokenRepository.revokeAllVerificationTokenOfUser(user.getUserId());
    }

    @Transactional
    @Override
    public Map<String, Object> resendEmailVerifyAccount(SendEmailRequest request) {
        String redisKey = "email:resend_cooldown:" + request.email();
        Boolean isFirstRequest = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "locked", COOLDOWN_RESEND_EMAIL_TIME, TimeUnit.SECONDS);
        // tránh null pointer exception
        if (Boolean.FALSE.equals(isFirstRequest)) {
            Long expireTime = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
            throw new CooldownResendException("Vui lòng đợi " + expireTime + " giây nữa để tiếp tục gửi lại mail.");
        }
        User user = userRepository.findByEmail(request.email()).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        if (user.getIsActive()) {
            throw new BusinessException(ErrorResponse.USER_ALREADY_ACTIVE);
        }
        verificationTokenRepository.revokeAllVerificationTokenOfUser(user.getUserId()); // revoke tất cả những token cũ
        VerificationToken verificationToken = VerificationToken.builder()
                .expiresAt(ZonedDateTime.now(ZoneOffset.UTC).plusHours(1))
                .token(UUID.randomUUID().toString())
                .user(user)
                .build();
        return createUserRegisterVerificationTokenAndOutboxEvent(user, verificationToken, EVENT_TYPE.RESEND_EMAIL);
    }

    @Transactional
    @Override
    public Map<String, Object> sendEmailResetPassword(SendEmailRequest request) {
        User user = userRepository.findByEmailAndIsActiveIsTrue(request.email()).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        String redisKey = "email-reset-password:send_cooldown:" + request.email();
        Boolean isFirstRequest = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "locked", COOLDOWN_EMAIL_RESET_PASSWORD_TIME, TimeUnit.SECONDS);
        // tránh null pointer exception
        if (Boolean.FALSE.equals(isFirstRequest)) {
            Long expireTime = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
            throw new CooldownResendException("Vui lòng đợi " + expireTime + " giây nữa để tiếp tục gửi lại mail.");
        }
        UserResetPasswordPayload payload = new UserResetPasswordPayload(user.getUserId(), request.email());
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .eventType(EVENT_TYPE.RESET_PASSWORD.name())
                .payload(jsonMapper.valueToTree(payload))
                .status(OutboxStatus.PENDING)
                .build();
        outboxEventRepository.save(outboxEvent);
        return CRUDResponseHelper.createSuccess();
    }

    @NonNull
    private Map<String, Object> createUserRegisterVerificationTokenAndOutboxEvent(User user, VerificationToken verificationToken, EVENT_TYPE eventType) {
        verificationTokenRepository.save(verificationToken);
        UserRegisteredPayload payload = new UserRegisteredPayload(
                user.getUserId(),
                user.getEmail(),
                verificationToken.getToken()
        );
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .eventType(eventType.name())
                .payload(jsonMapper.valueToTree(payload))
                .status(OutboxStatus.PENDING)
                .build();
        outboxEventRepository.save(outboxEvent);
        return CRUDResponseHelper.createSuccess();
    }

    private void checkVerifyToken(VerificationToken verification) {
        if (verification.getRevoked()) {
            throw new BusinessException(ErrorResponse.VERIFY_TOKEN_REVOKED);
        }
        if (verification.getExpiresAt()
                .isBefore(ZonedDateTime.now(ZoneOffset.UTC))) {
            verification.setRevoked(true);
            throw new BusinessException(ErrorResponse.VERIFY_TOKEN_EXPIRED);
        }
        verification.setVerified(true);
        verification.setRevoked(true);
    }

}
