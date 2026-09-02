package com.restaurant.waitlist.backend.entity;

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

    private String channel; // SMS, EMAIL

    private String audience; // segment name or JSON

    @Column(name = "template_id")
    private Long templateId;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "restaurant_id")
    private Long restaurantId;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    private String status; // DRAFT, SCHEDULED, ACTIVE, PAUSED, COMPLETED

    @Column(name = "sent_count")
    private Integer sentCount;

    private Integer reach;

    private Integer redemptions;

    @Column(name = "revenue_influenced")
    private BigDecimal revenueInfluenced;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
