package com.example.schedulemeetingbe;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

public class ClientTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(10);
        System.out.println(bCryptPasswordEncoder.encode("Aa123456@"));
        System.out.println("Instant: " + Instant.now());
        System.out.println("OffsetDateTime: " + OffsetDateTime.now());
        System.out.println("ZonedDateTime: " + ZonedDateTime.now());
    }
}
