package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.dto.response.ClaimReceiptResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ReceiptClaimService {
    /**
     * Claim points from a receipt upload
     */
    ClaimReceiptResponse claimReceipt(Long userId, Long restaurantId, MultipartFile file, String receiptAmount, String receiptDate) throws IOException;

    /**
     * Get pending receipts for admin approval
     */
    java.util.List<com.restaurant.waitlist.backend.entity.ReceiptClaim> getPendingReceipts(Long restaurantId, int page, int size);

    /**
     * Approve a receipt claim
     */
    void approveReceiptClaim(Long receiptClaimId, Long adminId);

    /**
     * Reject a receipt claim
     */
    void rejectReceiptClaim(Long receiptClaimId, Long adminId, String reason);

    /**
     * Detect duplicate receipts
     */
    boolean isDuplicateReceipt(Long userId, Long restaurantId, String amount, String date);

    /**
     * Get points for receipt (configurable by restaurant)
     */
    Long calculatePointsForReceipt(Long restaurantId);
}
