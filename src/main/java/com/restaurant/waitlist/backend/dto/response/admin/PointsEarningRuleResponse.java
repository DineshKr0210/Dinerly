package com.restaurant.waitlist.backend.dto.response.admin;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PointsEarningRuleResponse {
    private Long id;
    private String action;
    private Long pointsValue;
    private String description;
    private String icon;
    private Boolean clickable;
    private String actionUrl;
    private Long restaurantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
