package com.example.schedulemeetingbe.service.base;

import com.example.schedulemeetingbe.dto.request.user.CreateUserRequest;
import com.example.schedulemeetingbe.dto.request.user.UpdateAvatarRequest;
import com.example.schedulemeetingbe.dto.request.user.UpdateUserRequest;
import com.example.schedulemeetingbe.dto.response.UploadSignatureResponse;
import com.example.schedulemeetingbe.dto.response.UserDetailResponse;

import java.util.Map;

public interface IUserService {
    Map<String, Object> createUser(CreateUserRequest request);
    UserDetailResponse getUserDetail(Long id);
    UserDetailResponse updateUser(Long id, UpdateUserRequest request);
    Map<String, Object> updateEmail(Long id, String newEmail);
    Map<String, Object> lockAccount(Long id);
    Map<String, Object> unlockAccount(Long id);
    Map<String, Object> deleteForever(Long id);
    Map<String, Object> updateAvatar(Long id, UpdateAvatarRequest request);
    Map<String, Object> deleteAvatar(Long id);
    UploadSignatureResponse generateUploadSignature(Long id);
}
