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
@jakarta.persistence.Table(name = "restaurant_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "restaurant_id", nullable = false, unique = true)
    private Restaurant restaurant;

    @Column(nullable = false)
    @Builder.Default
    private Boolean sendSmsNotifications = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean sendEmailNotifications = true;

    private String nightlySummaryEmail;

    @Column(nullable = false)
    @Builder.Default
    private Integer averageServiceTime = 45;

    @Column(nullable = false)
    @Builder.Default
    private Integer bufferTime = 15;

    @Column(nullable = false)
    @Builder.Default
    private String operatingHours = "10:00-22:00";

    @Column(nullable = false)
    @Builder.Default
    private Integer maxWaitlistSize = 50;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

