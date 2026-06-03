package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.constant.enums.EVENT_TYPE;
import com.example.schedulemeetingbe.constant.enums.OutboxStatus;
import com.example.schedulemeetingbe.dto.common.CRUDResponseHelper;
import com.example.schedulemeetingbe.dto.request.CreateUserRequest;
import com.example.schedulemeetingbe.dto.response.UserDetailResponse;
import com.example.schedulemeetingbe.entity.OutboxEvent;
import com.example.schedulemeetingbe.entity.User;
import com.example.schedulemeetingbe.entity.payload.UserCreatePayload;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.exception.custom_exception.BusinessException;
import com.example.schedulemeetingbe.mapper.UserMapper;
import com.example.schedulemeetingbe.repository.OutboxEventRepository;
import com.example.schedulemeetingbe.repository.UserRepository;
import com.example.schedulemeetingbe.service.base.IEmailService;
import com.example.schedulemeetingbe.service.base.IUserService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JsonMapper jsonMapper;

    @Transactional
    @Override
    public Map<String, Object> createUser(CreateUserRequest request) {
        User user = User.builder()
                .username(request.username())
                .phone(request.phone() != null ? request.phone() : null)
                .email(request.email())
                .passwordHash(bCryptPasswordEncoder.encode(request.password()))
                .fullName(request.fullName())
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

    @Override
    public UserDetailResponse getUserDetail(Long id) {
        User user = userRepository.findById(id).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        return UserMapper.mapToUserDetailResponse(user);
    }
}
