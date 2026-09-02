package com.restaurant.waitlist.backend.dto.request.admin;

import lombok.Data;

import java.util.List;

@Data
public class MarketingCampaignRequest {
    private Long restaurantId; // optional, used for templating
    private String templateType; // optional
    private String message; // optional freeform message
    private List<String> phoneNumbers; // recipients
}
