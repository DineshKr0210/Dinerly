package com.restaurant.waitlist.backend.controller;

import com.restaurant.waitlist.backend.dto.request.UpdateSettingsProfileRequest;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.SettingsProfileResponse;
import com.restaurant.waitlist.backend.service.SettingsService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "bearerAuth")
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    @GetMapping("/{restaurantId}/profile")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<SettingsProfileResponse>> getProfileSettings(
            @PathVariable Long restaurantId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        try {
            SettingsProfileResponse response = settingsService.getProfileSettings(restaurantId, year, month);
            return ResponseEntity.ok(ApiResponse.success("Profile settings retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{restaurantId}/profile")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<SettingsProfileResponse>> updateProfileSettings(
            @PathVariable Long restaurantId,
            @RequestBody UpdateSettingsProfileRequest request) {
        try {
            SettingsProfileResponse response = settingsService.updateProfileSettings(restaurantId, request);
            return ResponseEntity.ok(ApiResponse.success("Profile settings updated", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
