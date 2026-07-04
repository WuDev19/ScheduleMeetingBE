package com.example.schedulemeetingbe.seeder.bootstrap;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.entity.Role;
import com.example.schedulemeetingbe.entity.User;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.exception.custom_exception.BusinessException;
import com.example.schedulemeetingbe.repository.RoleRepository;
import com.example.schedulemeetingbe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@RequiredArgsConstructor

public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_USERNAME}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    @Value("${ADMIN_EMAIL}")
    private String adminEmail;

    @Transactional
    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername(adminUsername)) {
            return;
        }
        Role adminRole = roleRepository.findByRoleName(StringCommon.ADMIN)
                .orElseThrow(() -> new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        User admin = User.builder()
                .username(adminUsername)
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .fullName("System Administrator")
                .isActive(true)
                .roles(Set.of(adminRole))
                .build();
        userRepository.save(admin);
    }
}
