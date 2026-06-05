package com.example.schedulemeetingbe.service.base;

import com.example.schedulemeetingbe.dto.request.CreateUserRequest;
import com.example.schedulemeetingbe.dto.request.UpdateUserRequest;
import com.example.schedulemeetingbe.dto.response.UserDetailResponse;

import java.util.Map;

public interface IUserService {
    Map<String, Object> createUser(CreateUserRequest request);
    UserDetailResponse getUserDetail(Long id);
    Map<String, Object> updateUser(Long id, UpdateUserRequest request);
    Map<String, Object> updateEmail(Long id, String newEmail);
}
