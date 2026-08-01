package com.restaurant.waitlist.backend.dto.response;

import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.entity.RestaurantSettings;
import com.restaurant.waitlist.backend.entity.WaitlistSettingsPayload;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RestaurantResponse {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String email;
    private String openTime;
    private String closeTime;
    private Integer totalTables;
    private Boolean walkInsOnly;
    private Boolean acceptOnlineJoin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RestaurantResponse fromRestaurant(Restaurant restaurant) {
        return fromRestaurant(restaurant, null);
    }

    public static RestaurantResponse fromRestaurant(Restaurant restaurant, RestaurantSettings settings) {
        WaitlistSettingsPayload waitlistSettings = settings != null ? settings.getWaitlistSettings() : WaitlistSettingsPayload.defaults();

        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .address(restaurant.getAddress())
                .phone(restaurant.getPhone())
                .email(restaurant.getEmail())
                .openTime(restaurant.getOpenTime())
                .closeTime(restaurant.getCloseTime())
                .totalTables(restaurant.getTotalTables())
                .walkInsOnly(waitlistSettings.getWalkInsOnly())
                .acceptOnlineJoin(waitlistSettings.getAcceptOnlineJoin())
                .createdAt(restaurant.getCreatedAt())
                .updatedAt(restaurant.getUpdatedAt())
                .build();
    }
}

