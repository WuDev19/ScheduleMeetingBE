package com.example.schedulemeetingbe.service.base;

import com.example.schedulemeetingbe.dto.request.CreateUserRequest;
import com.example.schedulemeetingbe.dto.response.UserDetailResponse;

import java.util.Map;

public interface IUserService {
    Map<String, Object> createUser(CreateUserRequest request);
    UserDetailResponse getUserDetail(Long id);
}
