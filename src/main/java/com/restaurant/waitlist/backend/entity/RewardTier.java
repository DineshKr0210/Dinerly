package com.restaurant.waitlist.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@jakarta.persistence.Table(name = "reward_tiers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardTier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false)
    private String name; // "Silver", "Gold", "Platinum"

    @Column(nullable = false, name = "points_threshold")
    private Long pointsThreshold; // Min points needed (0, 350, 700)

    @Column(nullable = false, name = "tier_order")
    private Integer tierOrder; // 1=Silver, 2=Gold, 3=Platinum

    @ElementCollection
    @CollectionTable(name = "tier_perks", joinColumns = @JoinColumn(name = "tier_id"))
    @Column(name = "perk")
    private List<String> perks; // ["Free dessert", "Priority seating"]

    @Column(name = "color")
    private String color; // For UI (silver, gold, platinum)

    @Column(columnDefinition = "TEXT") // Keep for backward compatibility
    private String perksJson;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
