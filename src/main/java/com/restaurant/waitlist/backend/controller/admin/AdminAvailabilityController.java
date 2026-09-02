package com.restaurant.waitlist.backend.controller.admin;

import com.restaurant.waitlist.backend.dto.request.UpdateRestaurantSettingsRequest;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.HolidayHoursResponse;
import com.restaurant.waitlist.backend.dto.response.RestaurantSettingsResponse;
import com.restaurant.waitlist.backend.dto.response.WaitlistSettingsResponse;
import com.restaurant.waitlist.backend.dto.request.HolidayHourRequest;
import com.restaurant.waitlist.backend.dto.request.UpdateHolidayHourRequest;
import com.restaurant.waitlist.backend.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/availability")
@RequiredArgsConstructor
public class AdminAvailabilityController {

    private final SettingsService settingsService;

    @GetMapping("/{restaurantId}/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RestaurantSettingsResponse>> getSettings(@PathVariable Long restaurantId) {
        RestaurantSettingsResponse resp = settingsService.getRestaurantSettings(restaurantId);
        return ResponseEntity.ok(ApiResponse.success("Settings retrieved", resp));
    }

    @PutMapping("/{restaurantId}/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RestaurantSettingsResponse>> updateSettings(@PathVariable Long restaurantId,
                                                                                  @RequestBody UpdateRestaurantSettingsRequest request) {
        RestaurantSettingsResponse resp = settingsService.updateRestaurantSettings(restaurantId, request);
        return ResponseEntity.ok(ApiResponse.success("Settings updated", resp));
    }

    @GetMapping("/{restaurantId}/waitlist-settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WaitlistSettingsResponse>> getWaitlistSettings(@PathVariable Long restaurantId) {
        WaitlistSettingsResponse resp = settingsService.getWaitlistSettings(restaurantId);
        return ResponseEntity.ok(ApiResponse.success("Waitlist settings retrieved", resp));
    }

    @PutMapping("/{restaurantId}/waitlist-settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WaitlistSettingsResponse>> updateWaitlistSettings(@PathVariable Long restaurantId,
                                                                                         @RequestBody com.restaurant.waitlist.backend.dto.request.UpdateWaitlistSettingsRequest request) {
        WaitlistSettingsResponse resp = settingsService.updateWaitlistSettings(restaurantId, request);
        return ResponseEntity.ok(ApiResponse.success("Waitlist settings updated", resp));
    }

    @GetMapping("/{restaurantId}/holiday-hours")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<HolidayHoursResponse>> getHolidayHours(@PathVariable Long restaurantId) {
        HolidayHoursResponse resp = settingsService.getHolidayHours(restaurantId);
        return ResponseEntity.ok(ApiResponse.success("Holiday hours retrieved", resp));
    }

    @PostMapping("/{restaurantId}/holiday-hours")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<com.restaurant.waitlist.backend.dto.response.HolidayHourResponse>> addHolidayHour(
            @PathVariable Long restaurantId,
            @RequestBody HolidayHourRequest request) {
        com.restaurant.waitlist.backend.dto.response.HolidayHourResponse resp = settingsService.addHolidayHour(restaurantId, request);
        return ResponseEntity.ok(ApiResponse.success("Holiday hour added", resp));
    }

    @PutMapping("/{restaurantId}/holiday-hours/{holidayHourId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<com.restaurant.waitlist.backend.dto.response.HolidayHourResponse>> updateHolidayHour(
            @PathVariable Long restaurantId,
            @PathVariable String holidayHourId,
            @RequestBody UpdateHolidayHourRequest request) {
        com.restaurant.waitlist.backend.dto.response.HolidayHourResponse resp = settingsService.updateHolidayHour(restaurantId, holidayHourId, request);
        return ResponseEntity.ok(ApiResponse.success("Holiday hour updated", resp));
    }
}
