package com.restaurant.waitlist.backend.dto.response;

import com.restaurant.waitlist.backend.entity.Restaurant;
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
    private Integer totalTables;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RestaurantResponse fromRestaurant(Restaurant restaurant) {
        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .address(restaurant.getAddress())
                .phone(restaurant.getPhone())
                .email(restaurant.getEmail())
                .totalTables(restaurant.getTotalTables())
                .createdAt(restaurant.getCreatedAt())
                .updatedAt(restaurant.getUpdatedAt())
                .build();
    }
}

