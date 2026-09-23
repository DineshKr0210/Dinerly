package com.restaurant.waitlist.backend.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
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

/**
 * Swagger UI is grouped into three journeys — Waitlist & Guest, Restaurant, then
 * Admin — via the GroupedOpenApi beans below, so the group switcher (top-right of
 * the UI) follows that order. Per-tag names/descriptions come entirely from the
 * @Tag annotation on each controller (see e.g. WaitlistController) — GroupedOpenApi
 * computes each group's own paths/tags independently from pathsToMatch, so a tag
 * list declared here on the shared OpenAPI bean is not honored per-group and was
 * removed after it produced empty, order-less tag sections in the UI.
 */
@Configuration
@io.swagger.v3.oas.annotations.security.SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Paste the JWT returned by /api/auth/login (no \"Bearer \" prefix needed)"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI dinerlyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Dinerly API")
                        .description("""
                                The complete Dinerly backend API — from a guest joining the waitlist, \
                                through restaurant staff running the floor, to admins managing their \
                                whole franchise group.

                                Endpoints are grouped below in the order guests and staff actually hit them: \
                                **Waitlist & Guest** first, then **Restaurant**, then **Admin**. Use the group \
                                switcher (top right) to jump between them.

                                Click **Authorize** and paste a JWT from `/api/auth/login` to try out secured \
                                endpoints right from this page.""")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Dinerly Technical Team")
                                .email("support@dinerly.com")
                                .url("https://dinerly.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local"),
                        new Server().url("https://apidev.dinerly.ca").description("Development"),
                        new Server().url("https://dinerly.ca").description("Production")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    @Bean
    public GroupedOpenApi waitlistAndGuestApi() {
        return GroupedOpenApi.builder()
                .group("1-waitlist-guest")
                .displayName("👋 Waitlist & Guest")
                .pathsToMatch("/api/waitlist/**", "/api/offers/**", "/api/rewards/**", "/api/menu/**",
                        "/api/feedback/**", "/api/auth/**", "/api/health/**", "/api/twilio/**")
                .build();
    }

    @Bean
    public GroupedOpenApi restaurantApi() {
        return GroupedOpenApi.builder()
                .group("2-restaurant")
                .displayName("🍽️ Restaurant")
                .pathsToMatch("/api/restaurants/**", "/api/settings/**", "/api/users/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("3-admin")
                .displayName("🛠️ Admin Console")
                .pathsToMatch("/api/admin/**")
                .build();
    }

    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("4-all-endpoints")
                .displayName("📘 All Endpoints")
                .pathsToMatch("/api/**")
                .build();
    }
}
