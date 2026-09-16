package com.restaurant.waitlist.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ClaimReceiptResponse {
    private Long pointsClaimed;
    private Long newBalance;
    private String receiptReference;
    private String status; // APPROVED, PENDING_REVIEW, REJECTED
    private String message;
    private LocalDateTime claimedAt;
}
