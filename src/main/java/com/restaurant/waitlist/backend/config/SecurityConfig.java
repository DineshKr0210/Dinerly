package com.restaurant.waitlist.backend.config;

import com.restaurant.waitlist.backend.security.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints
                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers("/api/auth/register", "/api/auth/verify-email", "/api/auth/resend-verification", "/api/auth/login", "/api/auth/forgot-password", "/api/auth/reset-password", "/api/auth/encode-password").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/swagger-docs", "/swagger-docs/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                        .requestMatchers("/api/waitlist/**").permitAll()
                        .requestMatchers("/api/twilio/**").permitAll()
                        
                        // Guest endpoints
                        .requestMatchers("/api/menu/**").hasAnyRole("GUEST", "ADMIN")
                        .requestMatchers("/api/feedback/**").hasAnyRole("GUEST", "ADMIN")
                        
                        // ✅ Restaurant staff endpoints (all restaurant roles)
                        .requestMatchers("/api/restaurants/**").hasAnyRole("STAFF", "HOST", "MANAGER", "OWNER", "ADMIN")
                        .requestMatchers("/api/tables/**").hasAnyRole("HOST", "MANAGER", "OWNER", "ADMIN")
                        
                        // ✅ Staff management (OWNER + ADMIN only)
                        .requestMatchers("/api/admin/staff/**").hasAnyRole("OWNER", "ADMIN")
                        
                        // ✅ System admin endpoints (ADMIN only)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        
                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

