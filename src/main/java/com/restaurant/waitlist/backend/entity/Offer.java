package com.restaurant.waitlist.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@jakarta.persistence.Table(name = "offers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Offer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "status")
    private String status; // ACTIVE, INACTIVE, DRAFT

    // Phase 1: Offer expansion fields
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type")
    private DiscountType discountType; // PERCENT, FIXED, FREE_ITEM, REWARDS_ELIGIBLE

    @Column(name = "discount_value")
    private BigDecimal discountValue;

    @Column(name = "discount_label")
    private String discountLabel; // "23% off", "$3 off"

    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    @CollectionTable(name = "offer_restrictions", joinColumns = @JoinColumn(name = "offer_id"))
    @Column(name = "restriction")
    private List<String> restrictions; // ["Dine-in only", "Sat-Sun only"]

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "rating_count")
    private Long ratingCount;

    @Column(name = "category")
    private String category; // PERCENT, FIXED, REWARDS_ELIGIBLE

    @Column(name = "per_user_limit")
    private Integer perUserLimit; // Max 2 per user

    @Column(name = "per_user_daily_limit")
    private Integer perUserDailyLimit; // Max 1 per day

    @Column(name = "inventory")
    private Integer inventory; // Available coupons

    @Column(name = "original_price")
    private BigDecimal originalPrice; // For strikethrough

    // Legacy fields (for backward compatibility)
    @Column(name = "value")
    private BigDecimal value;

    @Column(name = "points_cost")
    private Long pointsCost;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum DiscountType {
        PERCENT,           // 23% off
        FIXED,            // $3 off
        FREE_ITEM,        // Free item
        REWARDS_ELIGIBLE  // Earn points
    }
}
