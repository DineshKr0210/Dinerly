package com.restaurant.waitlist.backend.dto.request.admin;

import lombok.Data;

@Data
public class RedemptionRequest {
    private Long offerId;
    private String guestPhone;
    private String guestName;
    private Long restaurantId;
    private Long campaignId; // Track which campaign triggered this redemption
    private String redemptionCode;
}
