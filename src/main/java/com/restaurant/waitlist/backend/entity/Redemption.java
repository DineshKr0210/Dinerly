package com.restaurant.waitlist.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@jakarta.persistence.Table(name = "redemptions", uniqueConstraints = {
    @UniqueConstraint(columnNames = "redemption_code")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Redemption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "offer_id")
    private Offer offer;

    @Column(name = "reward_item_id")
    private Long rewardItemId;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(name = "guest_name")
    private String guestName;

    @Column(name = "guest_phone")
    private String guestPhone;

    @Column(name = "redeemed_at")
    @CreationTimestamp
    private LocalDateTime redeemedAt;

    @Column(name = "value")
    private BigDecimal value;

    // Phase 2: 6-digit redemption code fields
    @Column(name = "redemption_code", unique = true, length = 6)
    private String redemptionCode; // Auto-generated 6-digit code

    @Column(name = "code_expires_at")
    private LocalDateTime codeExpiresAt; // 1-hour TTL

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RedemptionStatus status; // GENERATED, COMPLETED, EXPIRED, CANCELLED

    @Column(name = "user_id")
    private Long userId; // Track which guest redeemed

    @ManyToOne
    @JoinColumn(name = "campaign_id")
    private Campaign campaign; // Track which campaign triggered this redemption

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum RedemptionStatus {
        GENERATED,      // Code generated, shown to guest
        COMPLETED,      // Staff entered code on POS
        EXPIRED,        // Code TTL exceeded
        CANCELLED       // Guest cancelled before use
    }
}
