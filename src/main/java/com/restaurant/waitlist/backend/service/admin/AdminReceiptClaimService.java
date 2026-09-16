package com.restaurant.waitlist.backend.service.admin;

import com.restaurant.waitlist.backend.dto.request.admin.ApproveReceiptRequest;
import com.restaurant.waitlist.backend.dto.request.admin.RejectReceiptRequest;
import com.restaurant.waitlist.backend.dto.response.admin.ReceiptClaimResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AdminReceiptClaimService {
    Page<ReceiptClaimResponse> listReceiptClaims(Long restaurantId, String status, LocalDateTime from, LocalDateTime to, Pageable pageable);
    
    Page<ReceiptClaimResponse> listPendingClaims(Long restaurantId, Pageable pageable);
    
    ReceiptClaimResponse getReceiptClaimById(Long claimId);
    
    ReceiptClaimResponse getReceiptClaimDetails(Long claimId);
    
    ReceiptClaimResponse approveReceiptClaim(Long claimId, ApproveReceiptRequest request);
    
    ReceiptClaimResponse rejectReceiptClaim(Long claimId, RejectReceiptRequest request);
    
    Map<String, Object> approveBulkClaims(List<Long> claimIds, Long pointsOverride);
    
    Map<String, Object> rejectBulkClaims(List<Long> claimIds, String reason);
    
    Page<ReceiptClaimResponse> getByUser(Long userId, Pageable pageable);
    
    Page<ReceiptClaimResponse> getByRestaurant(Long restaurantId, String status, Pageable pageable);
    
    Page<ReceiptClaimResponse> getDuplicateClaims(Long restaurantId, Long userId, Pageable pageable);
    
    void exportReceiptClaimsCsv(Long restaurantId, String status, LocalDateTime from, LocalDateTime to, ByteArrayOutputStream out);
    
    Map<String, Object> getStatistics(Long restaurantId, LocalDateTime from, LocalDateTime to);
    
    ReceiptClaimResponse markAsDuplicate(Long claimId, Long duplicateOfId);
    
    ReceiptClaimResponse revertClaim(Long claimId);
}
