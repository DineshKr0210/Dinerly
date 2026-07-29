package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.dto.request.UpdateRestaurantSettingsRequest;
import com.restaurant.waitlist.backend.dto.response.RestaurantSettingsResponse;
import com.restaurant.waitlist.backend.entity.NotificationSettingsPayload;
import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.entity.RestaurantSettings;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.RestaurantSettingsRepository;
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
class NotificationSettingsServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private WaitlistRepository waitlistRepository;

    @Mock
    private SmsService smsService;

    @Mock
    private RestaurantSettingsRepository restaurantSettingsRepository;

    @InjectMocks
    private SettingsService settingsService;

    @Test
    void updateRestaurantSettingsPersistsNotificationPayload() {
        Restaurant restaurant = Restaurant.builder().id(1L).name("Brothers Café").build();
        RestaurantSettings settings = RestaurantSettings.builder().id(10L).restaurant(restaurant).build();

        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(restaurantSettingsRepository.findByRestaurantId(1L)).thenReturn(Optional.of(settings));
        when(restaurantSettingsRepository.save(any(RestaurantSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationSettingsPayload payload = NotificationSettingsPayload.builder()
                .guestNotifications(NotificationSettingsPayload.GuestNotifications.builder()
                        .notifysmsenabled(false)
                        .notifycallenabled(true)
                        .approvesmsenabled(true)
                        .joinedwaitlistsmsenabled(false)
                        .build())
                .messageTemplates(NotificationSettingsPayload.MessageTemplates.builder()
                        .notifySmsTemplatePreview("Notify template")
                        .approveSmsTemplatePreview("Approve template")
                        .voice("female")
                        .build())
                .staffNotifications(NotificationSettingsPayload.StaffNotifications.builder()
                        .partyWaitingTooLong(true)
                        .tableOccupiedTooLong(false)
                        .build())
                .build();

        UpdateRestaurantSettingsRequest request = new UpdateRestaurantSettingsRequest();
        request.setNotificationSettings(payload);

        RestaurantSettingsResponse response = settingsService.updateRestaurantSettings(1L, request);

        assertNotNull(response);
        assertFalse(response.getNotificationSettings().getGuestNotifications().getNotifysmsenabled());
        assertTrue(response.getNotificationSettings().getGuestNotifications().getNotifycallenabled());
        assertEquals("female", response.getNotificationSettings().getMessageTemplates().getVoice());
        verify(restaurantSettingsRepository).save(any(RestaurantSettings.class));
    }
}
