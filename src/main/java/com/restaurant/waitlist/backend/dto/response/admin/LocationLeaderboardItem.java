package com.restaurant.waitlist.backend.dto.response.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LocationLeaderboardItem {
    private Long restaurantId;
    private String name;
    private Long joins;
}
