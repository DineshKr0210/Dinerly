package com.restaurant.waitlist.backend.entity;

import com.restaurant.waitlist.backend.converter.AudienceTypeConverter;
import com.restaurant.waitlist.backend.enums.AudienceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@jakarta.persistence.Table(name = "campaigns")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String channels; // JSON array: ["SMS", "EMAIL", "PUSH"] or single value for backward compat

    @Column(name = "audience")
    @Convert(converter = AudienceTypeConverter.class)
    private AudienceType audience; // ALL, RECENT_30D, LAPSED_30D, GOLD_PLATINUM, etc.

    @Column(name = "template_id")
    private Long templateId;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "restaurant_id")
    private Long restaurantId;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    private String status; // DRAFT, SCHEDULED, ACTIVE, PAUSED, ENDED

    @Column(name = "sent_count")
    private Integer sentCount;

    private Integer reach;

    private Integer redemptions;

    @Column(name = "revenue_influenced")
    private BigDecimal revenueInfluenced;

    @Column(name = "has_redemption_code")
    private Boolean hasRedemptionCode = false; // true = generate codes, false = no codes

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
