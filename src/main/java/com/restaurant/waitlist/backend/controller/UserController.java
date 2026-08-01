package com.restaurant.waitlist.backend.controller;

import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.UserResponse;
import com.restaurant.waitlist.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PreAuthorize("hasRole('RESTAURANT')")
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        try {
            return ResponseEntity.ok(ApiResponse.success("Users fetched successfully", userService.getAllUsers()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('RESTAURANT')")
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId) {
        try {
            userService.softDeleteUser(userId);
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
