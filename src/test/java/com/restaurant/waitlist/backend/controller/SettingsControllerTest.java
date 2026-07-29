package com.restaurant.waitlist.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.waitlist.backend.dto.request.UpdateRestaurantSettingsRequest;
import com.restaurant.waitlist.backend.dto.request.UpdateWaitlistSettingsRequest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SettingsControllerTest {

    private final SettingsController controller = new SettingsController();

    @Test
    void parseNotificationSettingsRequest_acceptsWrappedResponsePayload() {
        Map<String, Object> payload = Map.of(
                "success", true,
                "message", "Notification settings retrieved",
                "data", Map.of(
                        "id", 1,
                        "restaurantId", 1,
                        "averageServiceTime", 45,
                        "bufferTime", 15,
                        "maxWaitlistSize", 50,
                        "notificationSettings", Map.of(
                                "guestNotifications", Map.of("notifysmsenabled", true),
                                "messageTemplates", Map.of(),
                                "staffNotifications", Map.of()
                        )
                )
        );

        UpdateRestaurantSettingsRequest request = controller.parseNotificationSettingsRequest(payload);

        assertNotNull(request);
        assertEquals(45, request.getAverageServiceTime());
        assertEquals(15, request.getBufferTime());
        assertEquals(50, request.getMaxWaitlistSize());
        assertNotNull(request.getNotificationSettings());
    }

    @Test
    void updateWaitlistSettingsRequest_acceptsNestedPayload() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        UpdateWaitlistSettingsRequest request = mapper.readValue(
                "{\"waitlistSettings\":{\"maxPartySize\":4,\"walkInsOnly\":true,\"acceptOnlineJoin\":false}}",
                UpdateWaitlistSettingsRequest.class
        );

        assertNotNull(request);
        assertEquals(4, request.getMaxPartySize());
        assertEquals(true, request.getWalkInsOnly());
        assertEquals(false, request.getAcceptOnlineJoin());
    }
}
