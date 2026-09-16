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
@jakarta.persistence.Table(name = "receipt_claims")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptClaim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "user_id")
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false, name = "file_url")
    private String fileUrl; // S3 path

    @Column(nullable = false, name = "points_claimed")
    private Long pointsClaimed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus status; // UPLOADED, APPROVED, REJECTED, DUPLICATE

    @Column(columnDefinition = "TEXT", name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "receipt_amount")
    private String receiptAmount; // Amount from receipt if detected

    @Column(name = "receipt_date")
    private String receiptDate; // Date from receipt if detected

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_by")
    private Long approvedBy; // Admin who approved

    public enum ClaimStatus {
        UPLOADED,       // Waiting for approval
        APPROVED,       // Points credited
        REJECTED,       // Rejected by admin
        DUPLICATE       // Already claimed
    }
}
