package com.example.schedulemeetingbe.service.base;

public interface IEmailService {
    void sendEmailActiveAccount(String email, String token);
    void sendEmailResetPassword(String email);
    void sendEmailUsernamePassword(String email, String username, String password);
}
