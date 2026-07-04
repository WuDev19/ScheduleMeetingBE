package com.example.schedulemeetingbe.service.base;

import com.example.schedulemeetingbe.dto.request.user.CreateUserRequest;
import com.example.schedulemeetingbe.dto.request.user.UpdateAvatarRequest;
import com.example.schedulemeetingbe.dto.request.user.UpdateUserRequest;
import com.example.schedulemeetingbe.dto.response.PageResponse;
import com.example.schedulemeetingbe.dto.response.UploadSignatureResponse;
import com.example.schedulemeetingbe.dto.response.user.FullNameAndEmailResponse;
import com.example.schedulemeetingbe.dto.response.user.UserDetailResponse;
import com.example.schedulemeetingbe.entity.User;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

    void updatePassword(String email, String newPassword);

    PageResponse<UserDetailResponse> searchUser(String keyword, Pageable pageable);

    Optional<User> getDetail(Long id);

    PageResponse<FullNameAndEmailResponse> getFullNameAndEmail(Pageable pageable);

    Set<String> getMyRole(Long userId);

    List<User> getUserEmailIn(List<String> emails);

    Set<User> getUserUserIdIn(List<Long> ids);

    Set<User> getUserInDepartment(Long departmentId);
}
