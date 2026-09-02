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
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

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
    
    // Explicit getters in case Lombok annotation processing isn't active
    public Long getId() { return id; }
    public Offer getOffer() { return offer; }
    public Long getRestaurantId() { return restaurantId; }
    public String getGuestName() { return guestName; }
    public String getGuestPhone() { return guestPhone; }
    public LocalDateTime getRedeemedAt() { return redeemedAt; }
    public BigDecimal getValue() { return value; }

}
