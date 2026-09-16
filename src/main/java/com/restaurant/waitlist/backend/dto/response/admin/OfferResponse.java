package com.restaurant.waitlist.backend.dto.response.admin;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OfferResponse {
    private Long id;
    private String name;
    private Long locationId;
    private String locationName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;

    // Phase 1: New fields
    private String discountType;
    private BigDecimal discountValue;
    private String discountLabel;
    private String description;
    private List<String> restrictions;
    private String photoUrl;
    private Double rating;
    private Long ratingCount;
    private String category;
    private Integer perUserLimit;
    private Integer perUserDailyLimit;
    private Integer inventory;
    private BigDecimal originalPrice;

    // Legacy/Computed fields
    private BigDecimal value;
    private Long pointsCost;
    private Long redemptions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
