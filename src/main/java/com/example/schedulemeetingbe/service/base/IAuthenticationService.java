package com.example.schedulemeetingbe.service.base;

import com.example.schedulemeetingbe.dto.request.auth.LoginByUsernameRequest;
import com.example.schedulemeetingbe.dto.request.auth.LogoutRequest;
import com.example.schedulemeetingbe.dto.request.auth.SendEmailRequest;
import com.example.schedulemeetingbe.dto.request.auth.SignUpWithUsernameRequest;
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

    void verifyUpdateEmail(String token, String newEmail);

    Map<String, Object> resendEmailVerifyAccount(SendEmailRequest request);

    Map<String, Object> sendEmailResetPassword(SendEmailRequest request);
}
