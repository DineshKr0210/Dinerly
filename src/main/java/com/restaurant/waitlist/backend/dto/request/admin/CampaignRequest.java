package com.restaurant.waitlist.backend.dto.request.admin;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CampaignRequest {
    private String name;
    private String channel; // SMS or EMAIL
    private String audience; // e.g., ALL, RECENT_30D, LAPSED_30D, GOLD_PLATINUM
    private Long templateId;
    private String message;
    @JsonAlias({"locationId"})
    private Long restaurantId;
    private LocalDateTime scheduledAt; // optional

    public Long getLocationId() {
        return restaurantId;
    }

    public void setLocationId(Long locationId) {
        this.restaurantId = locationId;
    }
}
