package com.restaurant.waitlist.backend.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptClaimResponse {
    private Long id;
    private Long userId;
    private Long restaurantId;
    private String fileUrl;
    private Long pointsClaimed;
    private String status;  // UPLOADED, APPROVED, REJECTED, DUPLICATE
    private String rejectionReason;
    private String receiptAmount;
    private String receiptDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime approvedAt;
    private String approvedBy;
    private String userName;
    private String restaurantName;
}
