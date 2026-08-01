package com.restaurant.waitlist.backend.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@io.swagger.v3.oas.annotations.security.SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI dinerlyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Dinerly API Documentation")
                        .description("Complete API reference for all backend services.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Dinerly Technical Team")
                                .email("support@dinerly.com")
                                .url("https://dinerly.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Development"),
                        new Server().url("https://staging.dinerly.com").description("Staging"),
                        new Server().url("https://api.dinerly.com").description("Production")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new io.swagger.v3.oas.models.security.SecurityScheme()
                                .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Bearer token for secured restaurant APIs")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    @Bean
    public GroupedOpenApi authenticationApi() {
        return GroupedOpenApi.builder()
                .group("Authentication")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi restaurantApi() {
        return GroupedOpenApi.builder()
                .group("Restaurant")
                .pathsToMatch("/api/restaurants/**")
                .build();
    }

    @Bean
    public GroupedOpenApi waitlistApi() {
        return GroupedOpenApi.builder()
                .group("Waitlist")
                .pathsToMatch("/api/waitlist/**")
                .build();
    }

    @Bean
    public GroupedOpenApi settingsApi() {
        return GroupedOpenApi.builder()
                .group("Settings")
                .pathsToMatch("/api/settings/**")
                .build();
    }

    @Bean
    public GroupedOpenApi notificationApi() {
        return GroupedOpenApi.builder()
                .group("Notifications")
                .pathsToMatch("/api/notifications/**")
                .build();
    }

    @Bean
    public GroupedOpenApi paymentApi() {
        return GroupedOpenApi.builder()
                .group("Payments")
                .pathsToMatch("/api/payments/**")
                .build();
    }

    @Bean
    public GroupedOpenApi webhookApi() {
        return GroupedOpenApi.builder()
                .group("Webhooks")
                .pathsToMatch("/api/webhooks/**")
                .build();
    }
}
