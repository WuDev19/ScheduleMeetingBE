package com.example.schedulemeetingbe;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class ClientTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(10);
        System.out.println(bCryptPasswordEncoder.encode("Aa123456@"));;
    }
}
