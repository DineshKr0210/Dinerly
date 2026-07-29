package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.entity.NotificationSettingsPayload;
import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.entity.RestaurantSettings;
import com.restaurant.waitlist.backend.repository.RestaurantSettingsRepository;
import com.restaurant.waitlist.backend.repository.SmsTemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmsTemplateServiceTest {

    @Mock
    private SmsTemplateRepository smsTemplateRepository;

    @Mock
    private RestaurantSettingsRepository restaurantSettingsRepository;

    @InjectMocks
    private SmsTemplateService smsTemplateService;

    @Test
    void usesVoiceTemplatePreviewForCallNotifications() {
        Restaurant restaurant = Restaurant.builder().id(1L).name("Brothers Café").build();
        RestaurantSettings settings = RestaurantSettings.builder()
                .restaurant(restaurant)
                .notificationSettings(NotificationSettingsPayload.builder()
                        .messageTemplates(NotificationSettingsPayload.MessageTemplates.builder()
                                .voiceTemplatePreview("Call template for {guestName}")
                                .build())
                        .build())
                .build();

        when(restaurantSettingsRepository.findByRestaurantId(1L)).thenReturn(Optional.of(settings));

        String message = smsTemplateService.formatMessageForRestaurant(1L, "CALL_NOTIFICATION", java.util.Map.of("guestName", "Dinesh"));

        assertTrue(message.contains("Call template for Dinesh"));
    }
}
