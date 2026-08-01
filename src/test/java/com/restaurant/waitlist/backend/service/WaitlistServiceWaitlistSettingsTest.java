package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.dto.request.JoinWaitlistRequest;
import com.restaurant.waitlist.backend.entity.NotificationSettingsPayload;
import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.entity.RestaurantSettings;
import com.restaurant.waitlist.backend.entity.Waitlist;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

    @Test
    void joinWaitlistDoesNotSendSmsWhenJoinNotificationFlagIsDisabled() {
        Restaurant restaurant = Restaurant.builder().id(1L).name("Brothers Café").build();
        RestaurantSettings settings = RestaurantSettings.builder()
                .restaurant(restaurant)
                .waitlistSettings(WaitlistSettingsPayload.builder().build())
                .notificationSettings(NotificationSettingsPayload.builder()
                        .guestNotifications(NotificationSettingsPayload.GuestNotifications.builder()
                                .joinedwaitlistsmsenabled(false)
                                .build())
                        .build())
                .build();

        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(restaurantSettingsRepository.findByRestaurantId(1L)).thenReturn(Optional.of(settings));
        when(waitlistRepository.save(any(Waitlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JoinWaitlistRequest request = new JoinWaitlistRequest();
        request.setRestaurantId(1L);
        request.setName("Dana");
        request.setPhone("2045551234");
        request.setPartySize(2);

        waitlistService.joinWaitlist(request);

        verify(smsService, never()).sendJoinConfirmationSms(any(Long.class), any(String.class), any(String.class));
    }

    @Test
    void joinWaitlistSendsSmsWhenJoinNotificationFlagIsEnabled() {
        Restaurant restaurant = Restaurant.builder().id(1L).name("Brothers Café").build();
        RestaurantSettings settings = RestaurantSettings.builder()
                .restaurant(restaurant)
                .waitlistSettings(WaitlistSettingsPayload.builder().build())
                .notificationSettings(NotificationSettingsPayload.builder()
                        .guestNotifications(NotificationSettingsPayload.GuestNotifications.builder()
                                .joinedwaitlistsmsenabled(true)
                                .build())
                        .build())
                .build();

        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(restaurantSettingsRepository.findByRestaurantId(1L)).thenReturn(Optional.of(settings));
        when(waitlistRepository.save(any(Waitlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JoinWaitlistRequest request = new JoinWaitlistRequest();
        request.setRestaurantId(1L);
        request.setName("Dana");
        request.setPhone("2045551234");
        request.setPartySize(2);

        waitlistService.joinWaitlist(request);

        verify(smsService).sendJoinConfirmationSms(1L, "+912045551234", "Dana");
    }
}
