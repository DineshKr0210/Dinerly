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
    private Integer codesGenerated; // 1 once the shared coupon code has been generated, 0 otherwise
    private Boolean hasRedemptionCode; // Whether this campaign uses a redemption code
    private String redemptionCode; // The shared coupon code sent to every recipient (e.g. "BROTPIZZA50")
    private BigDecimal revenueInfluenced;
    private LocalDateTime createdAt;
}
