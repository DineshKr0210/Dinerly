package com.restaurant.waitlist.backend.dto.response;

import com.restaurant.waitlist.backend.entity.RestaurantSettings;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RestaurantSettingsResponse {
    private Long id;
    private Long restaurantId;
    private Boolean sendSmsNotifications;
    private Boolean sendEmailNotifications;
    private Integer averageServiceTime;
    private Integer bufferTime;
    private String operatingHours;
    private Integer maxWaitlistSize;

    public static RestaurantSettingsResponse fromSettings(RestaurantSettings settings) {
        return RestaurantSettingsResponse.builder()
                .id(settings.getId())
                .restaurantId(settings.getRestaurant().getId())
                .sendSmsNotifications(settings.getSendSmsNotifications())
                .sendEmailNotifications(settings.getSendEmailNotifications())
                .averageServiceTime(settings.getAverageServiceTime())
                .bufferTime(settings.getBufferTime())
                .operatingHours(settings.getOperatingHours())
                .maxWaitlistSize(settings.getMaxWaitlistSize())
                .build();
    }
}

