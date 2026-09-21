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
@jakarta.persistence.Table(name = "staff_permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "staff_id", nullable = false, unique = true)
    private Staff staff;

    @Column(nullable = false)
    @Builder.Default
    private boolean canManageOffers = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean canManageStaff = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean canViewReports = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean canManageSettings = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean canManageRewards = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean custom = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
