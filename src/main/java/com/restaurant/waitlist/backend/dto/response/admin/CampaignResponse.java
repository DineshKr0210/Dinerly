package com.restaurant.waitlist.backend.dto.response.admin;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CampaignResponse {
    private Long id;
    private String name;
    private String channels; // JSON array or single value
    private String channelDisplay; // e.g., "Email + push"
    private String audience;
    private Long templateId;
    private String message;
    private Long restaurantId;
    private Long locationId;
    private LocalDateTime scheduledAt;
    private LocalDateTime endDate;
    private String dateRangeDisplay; // e.g., "Jul 5 - Jul 31, 2026"
    private String status;
    private Integer sentCount;
    private Integer reach;
    private Integer redemptions;
    private Integer codesGenerated; // Number of codes generated for this campaign
    private Boolean hasRedemptionCode; // Whether this campaign uses redemption codes
    private BigDecimal revenueInfluenced;
    private LocalDateTime createdAt;
}
