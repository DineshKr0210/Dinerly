package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.dto.response.SettingsProfileResponse;
import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettingsServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private WaitlistRepository waitlistRepository;

    @Mock
    private SmsService smsService;

    @InjectMocks
    private SettingsService settingsService;

    @Test
    void getProfileSettingsReturnsRestaurantAndPlanDetails() {
        Restaurant restaurant = Restaurant.builder()
                .id(1L)
                .name("Brothers Café")
                .email("info@brotherscafe.ca")
                .phone("+1-204-555-0142")
                .address("123 Market St, Winnipeg")
                .openTime("11:00")
                .closeTime("22:00")
                .build();

        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(waitlistRepository.countSentSmsThisMonth(1L, 2026, 7)).thenReturn(142L);
        when(smsService.getCurrentMonthEstimatedCharge()).thenReturn(0.0);

        SettingsProfileResponse response = settingsService.getProfileSettings(1L, 2026, 7);

        assertNotNull(response);
        assertNotNull(response.getProfile());
        assertNotNull(response.getProfile().getRestaurant());
        assertEquals("Brothers Café", response.getProfile().getRestaurant().getName());
        assertEquals("11:00", response.getProfile().getRestaurant().getHours().getOpen());
        assertEquals("22:00", response.getProfile().getRestaurant().getHours().getClose());
        assertEquals("Basic", response.getProfile().getPlan().getName());
        assertEquals(142, response.getProfile().getPlan().getSmssentthismonth());
        assertEquals(0, response.getProfile().getPlan().getMarketingsmssentthismonth());
        assertEquals(0.0, response.getProfile().getPlan().getChargesthismonth());
    }
}
