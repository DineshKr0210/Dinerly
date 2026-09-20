package com.restaurant.waitlist.backend.dto.request.admin;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.restaurant.waitlist.backend.enums.AudienceType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CampaignRequest {
    private String name;
    private String channel; // SMS or EMAIL
    private AudienceType audience; // ALL, RECENT_30D, LAPSED_30D, GOLD_PLATINUM, etc.
    private Long templateId;
    private String message;
    private Long restaurantId;
    private LocalDateTime scheduledAt; // optional
    private LocalDateTime endDate; // optional
    private Boolean hasRedemptionCode; // true = generate codes at publish, false = no codes
}
