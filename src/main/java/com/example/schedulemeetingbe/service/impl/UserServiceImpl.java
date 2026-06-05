package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.constant.enums.EVENT_TYPE;
import com.example.schedulemeetingbe.constant.enums.OutboxStatus;
import com.example.schedulemeetingbe.dto.common.CRUDResponseHelper;
import com.example.schedulemeetingbe.dto.request.CreateUserRequest;
import com.example.schedulemeetingbe.dto.request.UpdateUserRequest;
import com.example.schedulemeetingbe.dto.response.UserDetailResponse;
import com.example.schedulemeetingbe.entity.OutboxEvent;
import com.example.schedulemeetingbe.entity.Role;
import com.example.schedulemeetingbe.entity.User;
import com.example.schedulemeetingbe.entity.VerificationToken;
import com.example.schedulemeetingbe.entity.payload.UserChangeEmailPayload;
import com.example.schedulemeetingbe.entity.payload.UserCreatePayload;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.exception.custom_exception.BusinessException;
import com.example.schedulemeetingbe.exception.custom_exception.CooldownResendException;
import com.example.schedulemeetingbe.mapper.UserMapper;
import com.example.schedulemeetingbe.repository.OutboxEventRepository;
import com.example.schedulemeetingbe.repository.RoleRepository;
import com.example.schedulemeetingbe.repository.UserRepository;
import com.example.schedulemeetingbe.repository.VerificationTokenRepository;
import com.example.schedulemeetingbe.service.base.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.example.schedulemeetingbe.constant.Constants.COOLDOWN_UPDATE_EMAIL;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JsonMapper jsonMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    @Override
    public Map<String, Object> createUser(CreateUserRequest request) {
        Role role = roleRepository.findByRoleName(request.role()).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        User user = User.builder()
                .username(request.username())
                .phone(request.phone() != null ? request.phone() : null)
                .email(request.email())
                .passwordHash(bCryptPasswordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .roles(Set.of(role))
                .isActive(true)
                .build();
        User saved = userRepository.save(user);
        UserCreatePayload payload = new UserCreatePayload(
                saved.getUserId(),
                saved.getEmail(),
                saved.getUsername(),
                request.password()
        );
        OutboxEvent event = OutboxEvent.builder()
                .eventType(EVENT_TYPE.CREATE_USER.name())
                .payload(jsonMapper.valueToTree(payload))
                .status(OutboxStatus.PENDING)
                .build();
        outboxEventRepository.save(event);
        return CRUDResponseHelper.createSuccess();
    }

    @Cacheable(value = "user-detail", key = "#id")
    @Override
    public UserDetailResponse getUserDetail(Long id) {
        User user = userRepository.findById(id).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        return UserMapper.mapToUserDetailResponse(user);
    }

    @Transactional
    @Override
    public Map<String, Object> updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        if (request.fullName() != null) {
            user.setFullName(request.fullName());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }
        if (request.username() != null) {
            user.setUsername(request.username());
        }
        return CRUDResponseHelper.updateSuccess();
    }

    @Transactional
    @Override
    public Map<String, Object> updateEmail(Long id, String newEmail) {
        String redisKey = "update_email:cooldown:" + id + ":" + newEmail;
        Boolean isFirstRequest = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "locked", COOLDOWN_UPDATE_EMAIL, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(isFirstRequest)) {
            Long expireTime = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
            throw new CooldownResendException("Bạn vừa gửi mail, vui lòng đợi " + expireTime + " giây nữa để tiếp tục gửi lại mail.");
        }
        User user = userRepository.findById(id).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        VerificationToken verificationToken = VerificationToken
                .builder()
                .token(UUID.randomUUID().toString())
                .expiresAt(ZonedDateTime.now().plusHours(1))
                .user(user)
                .build();
        verificationTokenRepository.save(verificationToken);
        UserChangeEmailPayload payload = new UserChangeEmailPayload(user.getUserId(), newEmail, verificationToken.getToken());
        OutboxEvent event = OutboxEvent.builder()
                .status(OutboxStatus.PENDING)
                .eventType(EVENT_TYPE.UPDATE_EMAIL.name())
                .payload(jsonMapper.valueToTree(payload))
                .build();
        outboxEventRepository.save(event);
        return CRUDResponseHelper.updateSuccess();
    }
}
