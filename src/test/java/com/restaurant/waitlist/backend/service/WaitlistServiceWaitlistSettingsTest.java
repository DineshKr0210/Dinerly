package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.dto.request.JoinWaitlistRequest;
import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.entity.RestaurantSettings;
import com.restaurant.waitlist.backend.entity.WaitlistSettingsPayload;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.RestaurantSettingsRepository;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WaitlistServiceWaitlistSettingsTest {

    @Mock
    private WaitlistRepository waitlistRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private RestaurantSettingsRepository restaurantSettingsRepository;

    @Mock
    private SmsService smsService;

    @InjectMocks
    private WaitlistService waitlistService;

    @Test
    void joinWaitlistRejectsPartySizesAboveConfiguredMaximum() {
        Restaurant restaurant = Restaurant.builder().id(1L).build();
        RestaurantSettings settings = RestaurantSettings.builder()
                .restaurant(restaurant)
                .waitlistSettings(WaitlistSettingsPayload.builder().maxPartySize(2).build())
                .build();

        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(restaurantSettingsRepository.findByRestaurantId(1L)).thenReturn(Optional.of(settings));

        JoinWaitlistRequest request = new JoinWaitlistRequest();
        request.setRestaurantId(1L);
        request.setName("Dana");
        request.setPhone("2045551234");
        request.setPartySize(3);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> waitlistService.joinWaitlist(request));

        assertTrue(ex.getMessage().contains("Party size"));
    }
}
