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
                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers("/api/auth/register", "/api/auth/verify-email", "/api/auth/resend-verification", "/api/auth/login", "/api/auth/forgot-password", "/api/auth/reset-password", "/api/auth/encode-password").permitAll()
                        .requestMatchers("/api/users/**").hasRole("RESTAURANT")
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                        // Allow guests to access waitlist public endpoints without authenticating
                        .requestMatchers("/api/waitlist/**").permitAll()
                        .requestMatchers("/api/twilio/**").permitAll()
                        .requestMatchers("/api/restaurants/**").hasRole("RESTAURANT")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

