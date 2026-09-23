package com.restaurant.waitlist.backend.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger UI is grouped into three journeys — Waitlist & Guest, Restaurant, then
 * Admin — via the GroupedOpenApi beans below, so the group switcher (top-right of
 * the UI) and the tag list within each group both follow that order rather than
 * springdoc's default alphabetical fallback.
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
                .tags(orderedTags())
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    private List<Tag> orderedTags() {
        return List.of(
                // --- Waitlist & Guest ---
                new Tag().name("Waitlist").description("Guest waitlist join, live status, and updates — the front door of the guest journey"),
                new Tag().name("Guest Offers").description("Browse restaurant offers and redeem them as a guest"),
                new Tag().name("Guest Rewards").description("Guest loyalty points, tiers, reward redemption, and receipt claims"),
                new Tag().name("Guest Menu").description("Browse restaurant menu categories, dishes, and types"),
                new Tag().name("Guest Feedback").description("Submit guest feedback and reviews after a visit"),
                new Tag().name("Auth").description("Registration, login, email verification, and password reset"),
                new Tag().name("System Health").description("Service liveness check"),
                new Tag().name("Twilio Webhooks").description("Inbound SMS/voice callbacks from Twilio (called by Twilio, not by API clients)"),
                // --- Restaurant ---
                new Tag().name("Restaurant").description("Staff dashboard: waitlist management, guest seating flow, and daily operations"),
                new Tag().name("Restaurant Tables").description("Manage a restaurant's tables: status, merging, and layout"),
                new Tag().name("Restaurant Staff").description("Manage a single location's staff accounts"),
                new Tag().name("Restaurant Notifications").description("Send SMS/call notifications to waitlist guests and view delivery history"),
                new Tag().name("Restaurant Redemptions").description("POS-side validation of offer, campaign, and reward codes"),
                new Tag().name("Restaurant Settings").description("Restaurant profile, hours, holiday schedule, and notification preferences"),
                new Tag().name("Restaurant Users").description("Manage user accounts (admin/manager/host)"),
                // --- Admin ---
                new Tag().name("Admin Dashboard").description("At-a-glance metrics, real-time stats, and insights feed for the admin console"),
                new Tag().name("Admin Staff").description("Manage staff across all of an admin's franchise locations"),
                new Tag().name("Admin Customers").description("Customer summary, list, and visit history across an admin's locations"),
                new Tag().name("Admin Locations").description("Manage franchise locations: list, create, and configure"),
                new Tag().name("Admin Settings").description("Restaurant settings management from the admin console"),
                new Tag().name("Admin Availability").description("Manage store hours, holidays, and closures across locations"),
                new Tag().name("Admin Reports").description("Generate, schedule, and download operational reports"),
                new Tag().name("Admin Performance").description("Waitlist, reviews, and rewards/offers performance analytics with period comparisons"),
                new Tag().name("Admin Reviews").description("View and reply to guest reviews, plus review analytics"),
                new Tag().name("Admin Marketing").description("SMS templates and one-off marketing sends"),
                new Tag().name("Admin Campaigns").description("Create and publish marketing campaigns to consenting guests"),
                new Tag().name("Admin Offers").description("Create, manage, and analyze restaurant offers"),
                new Tag().name("Admin Rewards").description("Loyalty program tiers and reward-earning settings"),
                new Tag().name("Admin Reward Items").description("Catalog of redeemable reward items"),
                new Tag().name("Admin Redemptions").description("View, cancel, and export offer/campaign/reward redemptions"),
                new Tag().name("Admin Receipt Claims").description("Review and approve/reject guest receipt claims for bonus points"),
                new Tag().name("Admin Points").description("Credit, debit, reverse, and audit guest loyalty points"),
                new Tag().name("Admin Points Rules").description("Configure how guests earn loyalty points"),
                new Tag().name("Admin Menu").description("Manage menu categories, dishes, and types")
        );
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
                .pathsToMatch("/**")
                .build();
    }
}
