package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.entity.User;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Scopes an admin to their own franchise group: their own restaurant plus any
 * restaurant whose {@code mainRestaurantId} points back to it. For a
 * standalone (non-franchise) restaurant this naturally resolves to just
 * itself, since nothing else references it as main.
 */
@Service
@RequiredArgsConstructor
public class AdminLocationAccessService {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    public Long getCurrentAdminRestaurantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        String email = principal instanceof UserDetails
                ? ((UserDetails) principal).getUsername()
                : principal != null ? principal.toString() : null;

        if (email == null || email.isBlank()) {
            return null;
        }

        return userRepository.findByEmail(email).map(User::getRestaurantId).orElse(null);
    }

    /**
     * Restaurant ids the current admin is allowed to see: their own restaurant
     * plus every franchise location under it. Empty if the admin has no
     * restaurant assigned.
     */
    public List<Long> getAccessibleRestaurantIds() {
        Long ownRestaurantId = getCurrentAdminRestaurantId();
        if (ownRestaurantId == null) {
            return Collections.emptyList();
        }
        return restaurantRepository.findByIdOrMainRestaurantId(ownRestaurantId).stream()
                .map(Restaurant::getId)
                .collect(Collectors.toList());
    }

    public boolean canAccessRestaurant(Long restaurantId) {
        if (restaurantId == null) {
            return true;
        }
        return getAccessibleRestaurantIds().contains(restaurantId);
    }

    /**
     * Throws if the current admin is not allowed to access the given
     * restaurant/location. A null restaurantId (meaning "all my locations")
     * always passes.
     */
    public void assertAccess(Long restaurantId) {
        if (restaurantId != null && !canAccessRestaurant(restaurantId)) {
            throw new AccessDeniedException("You do not have access to this location");
        }
    }

    /**
     * Resolves the restaurant id(s) a query should be scoped to, given the
     * (optional) locationId a client asked for. A specific id must belong to
     * the admin's franchise group (validated here); omitting it means "all of
     * my locations" and resolves to the admin's whole franchise group.
     */
    public List<Long> resolveRestaurantIds(Long requestedLocationId) {
        if (requestedLocationId != null) {
            assertAccess(requestedLocationId);
            return List.of(requestedLocationId);
        }
        return getAccessibleRestaurantIds();
    }
}
