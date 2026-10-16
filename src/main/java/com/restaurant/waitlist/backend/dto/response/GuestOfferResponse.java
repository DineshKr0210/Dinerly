package com.restaurant.waitlist.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class GuestOfferResponse {
    private Long id;
    private String name;
    private Long restaurantId;
    private String restaurantName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private BigDecimal discountValue;
    private String discountLabel;
    private String description;
    private String photoUrl;
    
    // Per-user redemption limit (how many times user can redeem this offer)
    private Integer perUserLimit;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Computed fields for guest
    private Boolean redeemable;
    private String reasonIfNotRedeemable;
    private Integer userRedemptionsTotal;
}
