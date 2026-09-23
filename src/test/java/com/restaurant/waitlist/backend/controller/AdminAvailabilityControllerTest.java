package com.restaurant.waitlist.backend.controller.admin;

import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.HolidayHourResponse;
import com.restaurant.waitlist.backend.dto.response.admin.AvailabilityResponse;
import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.service.AdminLocationAccessService;
import com.restaurant.waitlist.backend.service.SettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAvailabilityControllerTest {

    @Mock
    private SettingsService settingsService;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private AdminLocationAccessService adminLocationAccessService;

    private AdminAvailabilityController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminAvailabilityController(settingsService, restaurantRepository, adminLocationAccessService);
    }

    @Test
    void getAvailability_shouldApplyHolidayOverrideToMatchingRegularDate() {
        Restaurant restaurant = Restaurant.builder()
                .id(1L)
                .name("Brothers Cafe")
                .locationOpen(true)
                .openTime("11:00 AM")
                .closeTime("10:00 PM")
                .build();

        LocalDate today = LocalDate.now();
        LocalDate sunday = today.with(java.time.DayOfWeek.SUNDAY);

        when(restaurantRepository.findById(1L)).thenReturn(java.util.Optional.of(restaurant));
        when(settingsService.getHolidayHours(1L)).thenReturn(
                com.restaurant.waitlist.backend.dto.response.HolidayHoursResponse.builder()
                        .holidayHours(List.of(
                                HolidayHourResponse.builder()
                                        .date(sunday.toString())
                                        .openTime("11:00 AM")
                                        .closeTime("10:00 PM")
                                        .closed(true)
                                        .build()
                        ))
                        .build()
        );

        ResponseEntity<ApiResponse<AvailabilityResponse>> response = controller.getAvailability(1L);

        assertNotNull(response);
        assertTrue(response.getBody().isSuccess());
        AvailabilityResponse body = response.getBody().getData();
        assertNotNull(body);

        AvailabilityResponse.RegularHour sundayEntry = body.getRegularHours().stream()
                .filter(hour -> sunday.toString().equals(hour.getDate()))
                .findFirst()
                .orElseThrow();

        assertFalse(sundayEntry.getEnabled());
        assertEquals("11:00 AM", sundayEntry.getOpen());
        assertEquals("10:00 PM", sundayEntry.getClose());
    }
}
