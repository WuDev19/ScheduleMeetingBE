package com.example.schedulemeetingbe.service.base;

import com.example.schedulemeetingbe.dto.request.LoginByUsernameRequest;
import com.example.schedulemeetingbe.dto.request.LogoutRequest;
import com.example.schedulemeetingbe.dto.request.SignUpWithUsernameRequest;
import com.example.schedulemeetingbe.dto.response.LoginResponse;

import java.util.Map;

public interface IAuthenticationService {
    boolean checkAccessTokenInBlacklist(String tokenId);

    boolean checkUserExist(Long userId);
    LoginResponse refreshToken(String refToken);
    Map<String, Object> logout(LogoutRequest logoutRequest);
    LoginResponse login(LoginByUsernameRequest loginRequest);
    Map<String, Object> signUpWithUsername(SignUpWithUsernameRequest request);
    void verifyEmail(String token);
}
