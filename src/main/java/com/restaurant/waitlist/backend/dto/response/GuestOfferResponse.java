package com.restaurant.waitlist.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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

    // Phase 1: New fields for guest display
    private String discountType; // PERCENT, FIXED, FREE_ITEM, REWARDS_ELIGIBLE
    private BigDecimal discountValue;
    private String discountLabel; // "23% off", "$3 off", "Kids free"
    private String description;
    private List<String> restrictions; // ["Dine-in only", "Sat-Sun only"]
    private String photoUrl; // For grid card display
    private Double rating; // 4.7
    private Long ratingCount; // Number of reviews
    private String category; // For filtering: "% off", "$ off", "Rewards eligible"
    private BigDecimal originalPrice; // For strikethrough

    // Computed fields
    private BigDecimal currentPrice; // Calculated based on discount
    private Boolean redeemable; // Can user redeem this?
    private String reasonIfNotRedeemable; // Why can't they redeem
    private Integer remainingInventory; // How many left
    private Integer userRedemptionsToday; // How many times redeemed by user today
    private Integer userRedemptionsTotal; // Total by user

    // Legacy fields
    private BigDecimal value;
    private Long pointsCost;
}
