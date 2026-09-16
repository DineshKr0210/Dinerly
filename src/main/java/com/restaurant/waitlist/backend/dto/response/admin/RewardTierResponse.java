package com.restaurant.waitlist.backend.dto.response.admin;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RewardTierResponse {
    private Long id;
    private String name;
    private Long pointsThreshold;
    private Integer tierOrder;
    private List<String> perks;
    private String color;
    private Long restaurantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Legacy fields
    private Integer points;
}
