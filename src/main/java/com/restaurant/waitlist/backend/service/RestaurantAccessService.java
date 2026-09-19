package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.entity.User;
import com.restaurant.waitlist.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RestaurantAccessService {

    private final UserRepository userRepository;

    public boolean canAccess(Long restaurantId, UserDetails userDetails) {
        if (userDetails == null || userDetails.getUsername() == null) return false;
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return false;
        if (user.getRole() == User.UserRole.ADMIN) return true;
        if (user.getRole() == User.UserRole.GUEST) return false;
        return user.getRestaurantId() != null && user.getRestaurantId().equals(restaurantId);
    }

    public boolean isOwnerOf(Long restaurantId, UserDetails userDetails) {
        if (userDetails == null || userDetails.getUsername() == null) return false;
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return false;
        if (user.getRole() == User.UserRole.ADMIN) return true;
        return user.getRole() == User.UserRole.OWNER && 
               user.getRestaurantId() != null && 
               user.getRestaurantId().equals(restaurantId);
    }

    public boolean isManagerOrOwner(Long restaurantId, UserDetails userDetails) {
        if (userDetails == null || userDetails.getUsername() == null) return false;
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return false;
        if (user.getRole() == User.UserRole.ADMIN) return true;
        if (user.getRole() != User.UserRole.MANAGER && user.getRole() != User.UserRole.OWNER) return false;
        return user.getRestaurantId() != null && user.getRestaurantId().equals(restaurantId);
    }

    public boolean hasRoleLevel(User.UserRole requiredRole, User.UserRole userRole) {
        return getRoleLevel(userRole) >= getRoleLevel(requiredRole);
    }

    private int getRoleLevel(User.UserRole role) {
        switch (role) {
            case ADMIN: return 5;
            case OWNER: return 4;
            case MANAGER: return 3;
            case HOST: return 2;
            case STAFF: return 1;
            default: return 0;
        }
    }
}

