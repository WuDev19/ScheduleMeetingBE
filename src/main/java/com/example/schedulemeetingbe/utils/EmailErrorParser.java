package com.example.schedulemeetingbe.utils;

import com.example.schedulemeetingbe.exception.ErrorResponse;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;

import java.io.FileNotFoundException;

public class EmailErrorParser {

    private EmailErrorParser() {
    }

    public static ErrorResponse parseException(Exception ex) {
        if (ex instanceof MailAuthenticationException) {
            return ErrorResponse.SMTP_CONFIG_ERROR;
        }
        if (ex instanceof MailSendException) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "";

            //mã 550, 553 liên quan đến người nhận
            if (msg.contains("550") || msg.contains("553") || msg.toLowerCase().contains("address")) {
                return ErrorResponse.INVALID_RECIPIENT;
            }

            //liên quan đến kích thước file đính kèm email
            if (msg.toLowerCase().contains("size") || msg.toLowerCase().contains("limit")) {
                return ErrorResponse.ATTACHMENT_ERROR;
            }
        }
        if (ex.getCause() instanceof FileNotFoundException ||
                (ex.getMessage() != null && ex.getMessage().contains("FileNotFoundException"))) {
            return ErrorResponse.ATTACHMENT_ERROR;
        }
        return ErrorResponse.SYSTEM_UNKNOWN_ERROR;
    }

}
