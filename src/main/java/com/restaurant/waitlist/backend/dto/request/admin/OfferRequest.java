package com.restaurant.waitlist.backend.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class OfferRequest {
    @NotBlank
    private String name;

    @NotNull
    private Long locationId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private String status; // ACTIVE, INACTIVE, DRAFT

    // Phase 1: New fields for Offer expansion
    private String discountType; // PERCENT, FIXED, FREE_ITEM, REWARDS_ELIGIBLE
    private BigDecimal discountValue;
    private String discountLabel; // "23% off", "$3 off"
    private String description;
    private List<String> restrictions; // ["Dine-in only", "Sat-Sun only"]
    private String photoUrl;
    private Double rating;
    private Long ratingCount;
    private String category; // PERCENT, FIXED, REWARDS_ELIGIBLE
    private Integer perUserLimit;
    private Integer perUserDailyLimit;
    private Integer inventory;
    private BigDecimal originalPrice;

    // Legacy fields
    private BigDecimal value;
    private Long pointsCost;
}
