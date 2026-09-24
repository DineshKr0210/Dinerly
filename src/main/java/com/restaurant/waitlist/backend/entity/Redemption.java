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
@jakarta.persistence.Table(name = "redemptions")
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

    // Offer/reward codes: auto-generated 6-digit, unique per code (checked at app level).
    // Campaign codes: a single shared code reused across every guest who redeems it,
    // so this column is intentionally NOT globally unique.
    @Column(name = "redemption_code", length = 20)
    private String redemptionCode;

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
