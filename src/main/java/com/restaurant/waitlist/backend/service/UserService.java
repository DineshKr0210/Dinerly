package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.dto.response.UserResponse;
import com.restaurant.waitlist.backend.entity.User;
import com.restaurant.waitlist.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc().stream()
                .map(UserResponse::fromUser)
                .toList();
    }

    @Transactional
    public void softDeleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getDeletedAt() != null) {
            throw new RuntimeException("User is already deleted");
        }

        user.setDeletedAt(LocalDateTime.now());
        user.setEnabled(false);
        user.setEmailVerified(false);
        userRepository.save(user);
    }
}
