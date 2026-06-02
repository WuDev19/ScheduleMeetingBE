package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.dto.request.CreateUserRequest;
import com.example.schedulemeetingbe.dto.response.UserDetailResponse;
import com.example.schedulemeetingbe.service.base.IUserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@AllArgsConstructor
public class UserServiceImpl implements IUserService {

    @Override
    public Map<String, Object> createUser(CreateUserRequest request) {
        return Map.of();
    }

    @Override
    public UserDetailResponse getUserDetail(Long id) {
        return null;
    }
}
