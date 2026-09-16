package com.restaurant.waitlist.backend.dto.response.admin;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RewardItemResponse {
    private Long id;
    private String title;
    private String description;
    private Long pointsCost;
    private String icon;
    private String category;
    private Boolean available;
    private Long restaurantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
