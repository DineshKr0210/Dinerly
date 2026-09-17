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
    // Basic Info
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String email;
    private String managerEmail;
    private String ownerEmail;
    
    // Hours & Settings
    private String openTime;
    private String closeTime;
    private Boolean locationOpen;
    private Integer seats;
    private Integer totalTables;
    private Boolean walkInsOnly;
    private Boolean acceptOnlineJoin;
    
    // Real-time Status (MUST HAVE)
    private Boolean isOpen;
    private Boolean acceptingNewGuests;
    private Integer occupiedTables;
    private Integer availableTables;
    private Integer totalWaitlistGuests;
    
    // Analytics (SHOULD HAVE)
    private Integer averageWaitTime;
    private Integer currentSeatingTime;
    private Double restaurantRating;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static RestaurantResponse fromRestaurant(Restaurant restaurant) {
        return fromRestaurant(restaurant, null, false, 0, 0, 0, 0, 0, 0.0);
    }

    public static RestaurantResponse fromRestaurant(Restaurant restaurant, RestaurantSettings settings) {
        return fromRestaurant(restaurant, settings, false, 0, 0, 0, 0, 0, 0.0);
    }
    
    public static RestaurantResponse fromRestaurant(
            Restaurant restaurant,
            RestaurantSettings settings,
            Boolean isOpen,
            Integer occupiedTables,
            Integer availableTables,
            Integer totalWaitlistGuests,
            Integer averageWaitTime,
            Integer currentSeatingTime,
            Double restaurantRating) {
        
        WaitlistSettingsPayload waitlistSettings = settings != null ? settings.getWaitlistSettings() : WaitlistSettingsPayload.defaults();
        Boolean acceptOnlineJoin = waitlistSettings.getAcceptOnlineJoin();
        Boolean walkInsOnly = waitlistSettings.getWalkInsOnly();
        Boolean locationOpen = restaurant.getLocationOpen() != null ? restaurant.getLocationOpen() : true;
        
        // acceptingNewGuests = isOpen AND acceptOnlineJoin AND locationOpen
        Boolean acceptingNewGuests = (isOpen != null && isOpen) && 
                (acceptOnlineJoin != null && acceptOnlineJoin) && 
                (locationOpen != null && locationOpen);

        return RestaurantResponse.builder()
                // Basic Info
                .id(restaurant.getId())
                .name(restaurant.getName())
                .address(restaurant.getAddress())
                .phone(restaurant.getPhone())
                .email(restaurant.getEmail())
                .managerEmail(restaurant.getManagerEmail())
                .ownerEmail(restaurant.getOwnerEmail())
                // Hours & Settings
                .openTime(restaurant.getOpenTime())
                .closeTime(restaurant.getCloseTime())
                .locationOpen(locationOpen)
                .seats(restaurant.getSeats())
                .totalTables(restaurant.getTotalTables())
                .walkInsOnly(walkInsOnly)
                .acceptOnlineJoin(acceptOnlineJoin)
                // Real-time Status
                .isOpen(isOpen)
                .acceptingNewGuests(acceptingNewGuests)
                .occupiedTables(occupiedTables != null ? occupiedTables : 0)
                .availableTables(availableTables != null ? availableTables : 0)
                .totalWaitlistGuests(totalWaitlistGuests != null ? totalWaitlistGuests : 0)
                // Analytics
                .averageWaitTime(averageWaitTime != null ? averageWaitTime : 0)
                .currentSeatingTime(currentSeatingTime != null ? currentSeatingTime : 0)
                .restaurantRating(restaurantRating != null ? restaurantRating : 0.0)
                // Timestamps
                .createdAt(restaurant.getCreatedAt())
                .updatedAt(restaurant.getUpdatedAt())
                .build();
    }
}

