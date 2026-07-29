package com.restaurant.waitlist.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.waitlist.backend.dto.request.AdvancedSettingsRequest;
import com.restaurant.waitlist.backend.dto.request.HolidayHourRequest;
import com.restaurant.waitlist.backend.dto.request.UpdateHolidayHourRequest;
import com.restaurant.waitlist.backend.dto.request.UpdateRestaurantSettingsRequest;
import com.restaurant.waitlist.backend.dto.request.UpdateSettingsProfileRequest;
import com.restaurant.waitlist.backend.dto.request.UpdateWaitlistSettingsRequest;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.AdvancedSettingsResponse;
import com.restaurant.waitlist.backend.dto.response.HolidayHourResponse;
import com.restaurant.waitlist.backend.dto.response.HolidayHoursResponse;
import com.restaurant.waitlist.backend.dto.response.RestaurantSettingsResponse;
import com.restaurant.waitlist.backend.dto.response.SettingsProfileResponse;
import com.restaurant.waitlist.backend.dto.response.WaitlistSettingsResponse;
import com.restaurant.waitlist.backend.service.SettingsService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    @GetMapping("/{restaurantId}/notifications")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<RestaurantSettingsResponse>> getNotificationSettings(@PathVariable Long restaurantId) {
        try {
            RestaurantSettingsResponse response = settingsService.getRestaurantSettings(restaurantId);
            return ResponseEntity.ok(ApiResponse.success("Notification settings retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{restaurantId}/waitlist-settings")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<WaitlistSettingsResponse>> getWaitlistSettings(@PathVariable Long restaurantId) {
        try {
            WaitlistSettingsResponse response = settingsService.getWaitlistSettings(restaurantId);
            return ResponseEntity.ok(ApiResponse.success("Waitlist settings retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{restaurantId}/notifications")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<RestaurantSettingsResponse>> updateNotificationSettings(
            @PathVariable Long restaurantId,
            @RequestBody UpdateRestaurantSettingsRequest request) {
        try {
            RestaurantSettingsResponse response = settingsService.updateRestaurantSettings(restaurantId, request);
            return ResponseEntity.ok(ApiResponse.success("Notification settings updated", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{restaurantId}/waitlist-settings")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<WaitlistSettingsResponse>> updateWaitlistSettings(
            @PathVariable Long restaurantId,
            @RequestBody UpdateWaitlistSettingsRequest request) {
        try {
            WaitlistSettingsResponse response = settingsService.updateWaitlistSettings(restaurantId, request);
            return ResponseEntity.ok(ApiResponse.success("Waitlist settings updated", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{restaurantId}/advanced")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<AdvancedSettingsResponse>> getAdvancedSettings(@PathVariable Long restaurantId) {
        try {
            AdvancedSettingsResponse response = settingsService.getAdvancedSettings(restaurantId);
            return ResponseEntity.ok(ApiResponse.success("Advanced settings retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{restaurantId}/advanced")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<AdvancedSettingsResponse>> updateAdvancedSettings(
            @PathVariable Long restaurantId,
            @RequestBody AdvancedSettingsRequest request) {
        try {
            AdvancedSettingsResponse response = settingsService.updateAdvancedSettings(restaurantId, request);
            return ResponseEntity.ok(ApiResponse.success("Advanced settings updated", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping(value = "/{restaurantId}/qr-code", produces = MediaType.IMAGE_PNG_VALUE)
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<byte[]> getQrCode(@PathVariable Long restaurantId) {
        try {
            byte[] imageBytes = settingsService.getOrCreateQrCodeImage(restaurantId);
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(imageBytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{restaurantId}/qr-code")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteQrCode(@PathVariable Long restaurantId) {
        try {
            Map<String, Object> response = settingsService.deleteQrCode(restaurantId);
            return ResponseEntity.ok(ApiResponse.success("QR code deleted", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{restaurantId}/holiday-hours")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<HolidayHoursResponse>> getHolidayHours(@PathVariable Long restaurantId) {
        try {
            HolidayHoursResponse response = settingsService.getHolidayHours(restaurantId);
            return ResponseEntity.ok(ApiResponse.success("Holiday hours retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{restaurantId}/holiday-hours")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<HolidayHourResponse>> addHolidayHour(
            @PathVariable Long restaurantId,
            @RequestBody HolidayHourRequest request) {
        try {
            HolidayHourResponse response = settingsService.addHolidayHour(restaurantId, request);
            return ResponseEntity.ok(ApiResponse.success("Holiday hour added", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{restaurantId}/holiday-hours/{holidayHourId}")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<HolidayHourResponse>> updateHolidayHour(
            @PathVariable Long restaurantId,
            @PathVariable String holidayHourId,
            @RequestBody UpdateHolidayHourRequest request) {
        try {
            HolidayHourResponse response = settingsService.updateHolidayHour(restaurantId, holidayHourId, request);
            return ResponseEntity.ok(ApiResponse.success("Holiday hour updated", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    UpdateRestaurantSettingsRequest parseNotificationSettingsRequest(Object body) {
        if (body == null) {
            return new UpdateRestaurantSettingsRequest();
        }

        if (body instanceof Map<?, ?> map) {
            Object payloadNode = map.containsKey("data") && map.get("data") instanceof Map<?, ?>
                    ? map.get("data")
                    : body;

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.convertValue(payloadNode, UpdateRestaurantSettingsRequest.class);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(body, UpdateRestaurantSettingsRequest.class);
    }
}
