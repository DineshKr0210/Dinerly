package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.entity.User;
import com.restaurant.waitlist.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolves the authenticated user's id from the SecurityContext (the
 * principal is the user's email, set by JwtFilter). Was previously
 * duplicated identically in GuestOfferController and GuestRewardController.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CurrentUserResolver {

    private final UserRepository userRepository;

    public Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }

            // Get the email from the principal (it's set by JwtFilter)
            String email = authentication.getPrincipal().toString();

            // Look up the user by email to get their ID
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                log.debug("User not found for email: {}", email);
                return null;
            }

            return user.getId();
        } catch (Exception e) {
            log.debug("Error getting current user", e);
        }
        return null;
    }

    /**
     * Resolves the Staff.id linked to the currently authenticated user, if any
     * (e.g. for a logged-in SERVER wanting to see "their own" assigned tables).
     */
    public Long getCurrentStaffId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }
            String email = authentication.getPrincipal().toString();
            User user = userRepository.findByEmail(email).orElse(null);
            return user != null ? user.getStaffId() : null;
        } catch (Exception e) {
            log.debug("Error getting current staff id", e);
        }
        return null;
    }
}
