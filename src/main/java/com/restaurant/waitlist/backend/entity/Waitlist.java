package com.restaurant.waitlist.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@jakarta.persistence.Table(name = "waitlist")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Waitlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "guest_name", nullable = false)
    private String guestName;

    @Column(name = "guest_phone", nullable = false)
    private String guestPhone;

    @Column(name = "party_size", nullable = false)
    private Integer partySize;

    private String preference;

    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private WaitlistStatus status = WaitlistStatus.PENDING;

    @Column(name = "position")
    private Integer position;

    @Column(name = "estimated_wait_time")
    private Integer estimatedWaitTime;

    @Column(name = "joined_at")
    @CreationTimestamp
    private LocalDateTime joinedAt;

    @Column(name = "seated_at")
    private LocalDateTime seatedAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum WaitlistStatus {
        PENDING,      // Guest has joined, awaiting restaurant approval
        WAITING,      // Restaurant has approved, guest is waiting
        NOTIFIED,     // Guest has been notified to come
        SEATED,       // Guest has been seated
        CANCELLED,    // Guest cancelled
        NO_SHOW       // Guest didn't show up
    }
}

