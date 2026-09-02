package com.restaurant.waitlist.backend.dto.response.admin;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminDashboardResponse {
    private Long totalRestaurants;
    private Long totalWaitlistJoins;
    private Long totalActiveWaitlists;
    private Double averageRating;
    private List<LocationLeaderboardItem> topLocations;
}
