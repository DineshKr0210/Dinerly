package com.restaurant.waitlist.backend.controller.admin;

import com.restaurant.waitlist.backend.dto.request.UpdateRestaurantSettingsRequest;
import com.restaurant.waitlist.backend.dto.request.HolidayHourRequest;
import com.restaurant.waitlist.backend.dto.request.UpdateHolidayHourRequest;
import com.restaurant.waitlist.backend.dto.response.*;
import com.restaurant.waitlist.backend.dto.response.admin.AvailabilityResponse;
import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.service.AdminLocationAccessService;
import com.restaurant.waitlist.backend.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/availability")
@RequiredArgsConstructor
public class AdminAvailabilityController {

    private final SettingsService settingsService;
    private final RestaurantRepository restaurantRepository;
    private final AdminLocationAccessService adminLocationAccessService;

    @GetMapping("/{restaurantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> getAvailability(@PathVariable Long restaurantId) {
        adminLocationAccessService.assertAccess(restaurantId);
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        String openTime = restaurant.getOpenTime() != null ? restaurant.getOpenTime() : "07:00 AM";
        String closeTime = restaurant.getCloseTime() != null ? restaurant.getCloseTime() : "08:00 PM";

        List<HolidayHourResponse> holidayHours = settingsService.getHolidayHours(restaurantId).getHolidayHours();
        if (holidayHours == null) {
            holidayHours = List.of();
        }

        List<AvailabilityResponse.RegularHour> regularHours = new ArrayList<>();
        LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        for (int i = 0; i < DayOfWeek.values().length; i++) {
            DayOfWeek dayOfWeek = DayOfWeek.values()[i];
            LocalDate date = weekStart.plusDays(i);
            regularHours.add(AvailabilityResponse.RegularHour.builder()
                    .date(date.toString())
                    .day(dayOfWeek.name().substring(0, 1).toUpperCase() + dayOfWeek.name().substring(1).toLowerCase())
                    .open(openTime)
                    .close(closeTime)
                    .enabled(true)
                    .build());
        }

        Map<String, HolidayHourResponse> holidayByDate = new HashMap<>();
        for (HolidayHourResponse hour : holidayHours) {
            if (hour.getDate() != null && !hour.getDate().isBlank()) {
                holidayByDate.put(hour.getDate(), hour);
            }
        }

        List<AvailabilityResponse.SpecialHour> specialHours = holidayHours.stream()
                .filter(hour -> hour.getDate() != null && !hour.getDate().isBlank())
                .map(hour -> AvailabilityResponse.SpecialHour.builder()
                        .id(null)
                        .date(hour.getDate())
                        .open(hour.getOpenTime() != null ? hour.getOpenTime() : openTime)
                        .close(hour.getCloseTime() != null ? hour.getCloseTime() : closeTime)
                        .enabled(!Boolean.TRUE.equals(hour.getClosed()))
                        .build())
                .toList();

        for (AvailabilityResponse.RegularHour regularHour : regularHours) {
            HolidayHourResponse holidayHour = holidayByDate.get(regularHour.getDate());
            if (holidayHour == null) {
                continue;
            }
            regularHour.setEnabled(!Boolean.TRUE.equals(holidayHour.getClosed()));
            if (holidayHour.getOpenTime() != null && !holidayHour.getOpenTime().isBlank()) {
                regularHour.setOpen(holidayHour.getOpenTime());
            }
            if (holidayHour.getCloseTime() != null && !holidayHour.getCloseTime().isBlank()) {
                regularHour.setClose(holidayHour.getCloseTime());
            }
        }

        LocalDate today = LocalDate.now();
        boolean todayClosed = specialHours.stream().anyMatch(hour -> {
            try {
                return LocalDate.parse(hour.getDate()).equals(today) && Boolean.FALSE.equals(hour.getEnabled());
            } catch (Exception ignored) {
                return false;
            }
        });

        AvailabilityResponse response = AvailabilityResponse.builder()
                .locationId(restaurant.getId())
                .location(restaurant.getName())
                .currentlyOpen(Boolean.TRUE.equals(restaurant.getLocationOpen()) && !todayClosed)
                .regularHours(regularHours)
                .specialHours(specialHours)
                .build();

        return ResponseEntity.ok(ApiResponse.success("Availability retrieved", response));
    }

    @GetMapping("/{restaurantId}/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RestaurantSettingsResponse>> getSettings(@PathVariable Long restaurantId) {
        adminLocationAccessService.assertAccess(restaurantId);
        RestaurantSettingsResponse resp = settingsService.getRestaurantSettings(restaurantId);
        return ResponseEntity.ok(ApiResponse.success("Settings retrieved", resp));
    }

    @PutMapping("/{restaurantId}/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RestaurantSettingsResponse>> updateSettings(@PathVariable Long restaurantId,
                                                                                  @RequestBody UpdateRestaurantSettingsRequest request) {
        adminLocationAccessService.assertAccess(restaurantId);
        RestaurantSettingsResponse resp = settingsService.updateRestaurantSettings(restaurantId, request);
        return ResponseEntity.ok(ApiResponse.success("Settings updated", resp));
    }

    @GetMapping("/{restaurantId}/waitlist-settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WaitlistSettingsResponse>> getWaitlistSettings(@PathVariable Long restaurantId) {
        adminLocationAccessService.assertAccess(restaurantId);
        WaitlistSettingsResponse resp = settingsService.getWaitlistSettings(restaurantId);
        return ResponseEntity.ok(ApiResponse.success("Waitlist settings retrieved", resp));
    }

    @PutMapping("/{restaurantId}/waitlist-settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WaitlistSettingsResponse>> updateWaitlistSettings(@PathVariable Long restaurantId,
                                                                                         @RequestBody com.restaurant.waitlist.backend.dto.request.UpdateWaitlistSettingsRequest request) {
        adminLocationAccessService.assertAccess(restaurantId);
        WaitlistSettingsResponse resp = settingsService.updateWaitlistSettings(restaurantId, request);
        return ResponseEntity.ok(ApiResponse.success("Waitlist settings updated", resp));
    }

    @GetMapping("/{restaurantId}/holiday-hours")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<HolidayHoursResponse>> getHolidayHours(@PathVariable Long restaurantId) {
        adminLocationAccessService.assertAccess(restaurantId);
        HolidayHoursResponse resp = settingsService.getHolidayHours(restaurantId);
        return ResponseEntity.ok(ApiResponse.success("Holiday hours retrieved", resp));
    }

    @PostMapping("/{restaurantId}/holiday-hours")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<com.restaurant.waitlist.backend.dto.response.HolidayHourResponse>> addHolidayHour(
            @PathVariable Long restaurantId,
            @RequestBody HolidayHourRequest request) {
        adminLocationAccessService.assertAccess(restaurantId);
        com.restaurant.waitlist.backend.dto.response.HolidayHourResponse resp = settingsService.addHolidayHour(restaurantId, request);
        return ResponseEntity.ok(ApiResponse.success("Holiday hour added", resp));
    }

    @PutMapping("/{restaurantId}/holiday-hours/{holidayHourId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<com.restaurant.waitlist.backend.dto.response.HolidayHourResponse>> updateHolidayHour(
            @PathVariable Long restaurantId,
            @PathVariable String holidayHourId,
            @RequestBody UpdateHolidayHourRequest request) {
        adminLocationAccessService.assertAccess(restaurantId);
        com.restaurant.waitlist.backend.dto.response.HolidayHourResponse resp = settingsService.updateHolidayHour(restaurantId, holidayHourId, request);
        return ResponseEntity.ok(ApiResponse.success("Holiday hour updated", resp));
    }
}
