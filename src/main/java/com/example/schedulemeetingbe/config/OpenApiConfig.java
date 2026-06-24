package com.example.schedulemeetingbe.config;

import com.example.schedulemeetingbe.constant.StringCommon;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(StringCommon.APP_NAME_UPPER_CASE)
                        .version("1.0.0")
                        .description("Tài liệu hướng dẫn và thử nghiệm API dành cho " + StringCommon.APP_NAME_LOWER_CASE)
                        .contact(new Contact()
                                .name("Người phát triển - Nguyễn Văn Vũ")
                                .email("nguyenvu19a19@gmail.com")
                        )
                )
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        StringCommon.SECURITY_SCHEME,
                                        new SecurityScheme()
                                                .name(StringCommon.AUTHORIZATION)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                );
    }

    @Bean
    public GroupedOpenApi authGroup() {
        return GroupedOpenApi.builder()
                .group("Authentication")
                .pathsToMatch("/api/v1/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userGroup() {
        return GroupedOpenApi.builder()
                .group("User")
                .pathsToMatch("/api/v1/user/**")
                .build();
    }

    @Bean
    public GroupedOpenApi buildingGroup() {
        return GroupedOpenApi.builder()
                .group("Building")
                .pathsToMatch("/api/v1/building/**")
                .build();
    }

    @Bean
    public GroupedOpenApi roomGroup() {
        return GroupedOpenApi.builder()
                .group("Room")
                .pathsToMatch("/api/v1/room/**")
                .build();
    }

    @Bean
    public GroupedOpenApi unavailabilityRoomGroup() {
        return GroupedOpenApi.builder()
                .group("Unavailability Room")
                .pathsToMatch("/api/v1/unavailability-room/**")
                .build();
    }

    @Bean
    public GroupedOpenApi equipmentGroup() {
        return GroupedOpenApi.builder()
                .group("Equipment")
                .pathsToMatch("/api/v1/equipment/**")
                .build();
    }

    @Bean
    public GroupedOpenApi bookingGroup() {
        return GroupedOpenApi.builder()
                .group("Booking")
                .pathsToMatch("/api/v1/booking/**")
                .build();
    }

    @Bean
    public GroupedOpenApi recurringPatternGroup() {
        return GroupedOpenApi.builder()
                .group("Recurring Pattern")
                .pathsToMatch("/api/v1/recurring-pattern/**")
                .build();
    }

    @Bean
    public GroupedOpenApi notificationGroup() {
        return GroupedOpenApi.builder()
                .group("Notification")
                .pathsToMatch("/api/v1/notification/**")
                .build();
    }

}
