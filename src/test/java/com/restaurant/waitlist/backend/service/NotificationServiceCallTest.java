package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.dto.response.SendCallResponse;
import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.entity.Waitlist;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.RestaurantSettingsRepository;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceCallTest {

    @Mock
    private WaitlistRepository waitlistRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private SmsService smsService;

    @Mock
    private RestaurantSettingsRepository restaurantSettingsRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void makeCallPassesGuestNameToVoiceService() {
        Restaurant restaurant = Restaurant.builder().id(1L).name("Brothers Café").build();
        Waitlist waitlist = Waitlist.builder()
                .id(10L)
                .guestName("Dinesh")
                .guestPhone("+1234567890")
                .restaurant(restaurant)
                .build();

        when(waitlistRepository.findById(10L)).thenReturn(Optional.of(waitlist));
        when(restaurantSettingsRepository.findByRestaurantId(1L)).thenReturn(Optional.empty());
        when(smsService.makePhoneCall(eq(1L), eq("+1234567890"), anyString(), eq("Dinesh")))
                .thenReturn(SendCallResponse.builder().callInitiated(true).build());

        notificationService.makeCall(1L, 10L, null);

        verify(smsService).makePhoneCall(eq(1L), eq("+1234567890"), anyString(), eq("Dinesh"));
    }
}
