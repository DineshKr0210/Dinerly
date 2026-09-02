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
    private String channel;
    private String audience;
    private Long templateId;
    private String message;
    private Long restaurantId;
    private Long locationId;
    private LocalDateTime scheduledAt;
    private String status;
    private Integer sentCount;
    private Integer reach;
    private Integer redemptions;
    private BigDecimal revenueInfluenced;
    private LocalDateTime createdAt;
}
