package com.example.schedulemeetingbe.config;

import com.example.schedulemeetingbe.security.MyJwtDecoder;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Configuration
@EnableMethodSecurity
@AllArgsConstructor
public class SecurityConfig {
    private final MyJwtDecoder myJwtDecoder;
    private final String END_POINT = "/api/v1/";
    private final String[] publicEndpoints = {
            END_POINT + "auth/**",
            END_POINT + "public/**",
            END_POINT + "swagger-ui.html",
            END_POINT + "api-docs/**",
            END_POINT + "swagger-ui/**",
            END_POINT + "booking/attendee/confirm"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {
        return httpSecurity.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers(publicEndpoints).permitAll()
                                .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .oauth2ResourceServer(oAuth2 ->
                        oAuth2.jwt(jwtConfigurer ->
                                jwtConfigurer
                                        .decoder(myJwtDecoder)
                                        .jwtAuthenticationConverter(grantedAuthoritiesConverter())
                        )
                )
                .build();
    }

    //khai báo cho spring biết roles này có nghĩa, bởi vì nếu dùng oauth2 thì chỉ hiểu scope hoặc scp
    public JwtAuthenticationConverter grantedAuthoritiesConverter() {
        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Stream<GrantedAuthority> roles = jwt.getClaimAsStringList("roles")
                    .stream()
                    .map(role -> "ROLE_" + role)
                    .map(SimpleGrantedAuthority::new);
            Stream<GrantedAuthority> permissions = jwt.getClaimAsStringList("permissions")
                    .stream()
                    .map(SimpleGrantedAuthority::new);
            return Stream.concat(roles, permissions)
                    .toList();
        });
        return authenticationConverter;
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "http://localhost",
                "http://127.0.0.1",
                "https://*.ngrok-free.dev",
                "https://meeting-frontend.vercel.app"
        ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
