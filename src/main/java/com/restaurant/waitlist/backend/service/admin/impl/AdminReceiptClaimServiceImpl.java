package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.request.admin.ApproveReceiptRequest;
import com.restaurant.waitlist.backend.dto.request.admin.RejectReceiptRequest;
import com.restaurant.waitlist.backend.dto.response.admin.ReceiptClaimResponse;
import com.restaurant.waitlist.backend.entity.ReceiptClaim;
import com.restaurant.waitlist.backend.repository.ReceiptClaimRepository;
import com.restaurant.waitlist.backend.service.AdminLocationAccessService;
import com.restaurant.waitlist.backend.service.admin.AdminReceiptClaimService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminReceiptClaimServiceImpl implements AdminReceiptClaimService {
    private static final Logger log = LoggerFactory.getLogger(AdminReceiptClaimServiceImpl.class);

    private final ReceiptClaimRepository receiptClaimRepository;
    private final AdminLocationAccessService adminLocationAccessService;

    @Override
    public Page<ReceiptClaimResponse> listReceiptClaims(Long restaurantId, String status, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        log.info("Listing receipt claims - restaurantId: {}, status: {}", restaurantId, status);
        List<Long> restaurantIds = adminLocationAccessService.resolveRestaurantIds(restaurantId);
        Page<ReceiptClaim> claims = (status != null && !status.isBlank())
                ? receiptClaimRepository.findByRestaurantIdInAndStatus(restaurantIds, ReceiptClaim.ClaimStatus.valueOf(status), pageable)
                : receiptClaimRepository.findByRestaurantIdIn(restaurantIds, pageable);
        return claims.map(this::map);
    }

    @Override
    public Page<ReceiptClaimResponse> listPendingClaims(Long restaurantId, Pageable pageable) {
        log.info("Listing pending receipt claims - restaurantId: {}", restaurantId);
        List<Long> restaurantIds = adminLocationAccessService.resolveRestaurantIds(restaurantId);
        Page<ReceiptClaim> claims = receiptClaimRepository.findByRestaurantIdInAndStatus(restaurantIds, ReceiptClaim.ClaimStatus.UPLOADED, pageable);
        return claims.map(this::map);
    }

    @Override
    public ReceiptClaimResponse getReceiptClaimById(Long claimId) {
        log.info("Getting receipt claim - claimId: {}", claimId);
        ReceiptClaim claim = receiptClaimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Receipt claim not found"));
        adminLocationAccessService.assertAccess(claim.getRestaurant().getId());
        return map(claim);
    }

    @Override
    public ReceiptClaimResponse getReceiptClaimDetails(Long claimId) {
        log.info("Getting receipt claim details - claimId: {}", claimId);
        ReceiptClaim claim = receiptClaimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Receipt claim not found"));
        adminLocationAccessService.assertAccess(claim.getRestaurant().getId());
        return map(claim);
    }

    @Override
    public ReceiptClaimResponse approveReceiptClaim(Long claimId, ApproveReceiptRequest request) {
        log.info("Approving receipt claim - claimId: {}, points: {}", claimId, request.getPointsOverride());
        ReceiptClaim claim = receiptClaimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Receipt claim not found"));
        adminLocationAccessService.assertAccess(claim.getRestaurant().getId());

        claim.setStatus(ReceiptClaim.ClaimStatus.APPROVED);
        claim.setPointsClaimed(request.getPointsOverride());
        claim.setApprovedAt(LocalDateTime.now());
        claim.setApprovedBy(1L); // TODO: Get actual admin user ID from auth
        
        claim = receiptClaimRepository.save(claim);
        return map(claim);
    }

    @Override
    public ReceiptClaimResponse rejectReceiptClaim(Long claimId, RejectReceiptRequest request) {
        log.info("Rejecting receipt claim - claimId: {}, reason: {}", claimId, request.getReason());
        ReceiptClaim claim = receiptClaimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Receipt claim not found"));
        adminLocationAccessService.assertAccess(claim.getRestaurant().getId());

        claim.setStatus(ReceiptClaim.ClaimStatus.REJECTED);
        claim.setRejectionReason(request.getReason());
        
        claim = receiptClaimRepository.save(claim);
        return map(claim);
    }

    @Override
    public Map<String, Object> approveBulkClaims(List<Long> claimIds, Long pointsOverride) {
        log.info("Approving {} receipt claims in bulk", claimIds.size());
        int approved = 0;
        
        for (Long claimId : claimIds) {
            ReceiptClaim claim = receiptClaimRepository.findById(claimId).orElse(null);
            if (claim != null && ReceiptClaim.ClaimStatus.UPLOADED.equals(claim.getStatus())
                    && adminLocationAccessService.canAccessRestaurant(claim.getRestaurant().getId())) {
                claim.setStatus(ReceiptClaim.ClaimStatus.APPROVED);
                if (pointsOverride != null) {
                    claim.setPointsClaimed(pointsOverride);
                }
                claim.setApprovedAt(LocalDateTime.now());
                claim.setApprovedBy(1L); // TODO: Get actual admin user ID from auth
                receiptClaimRepository.save(claim);
                approved++;
            }
        }
        
        return Map.of(
            "totalRequested", claimIds.size(),
            "approved", approved
        );
    }

    @Override
    public Map<String, Object> rejectBulkClaims(List<Long> claimIds, String reason) {
        log.info("Rejecting {} receipt claims in bulk", claimIds.size());
        int rejected = 0;
        
        for (Long claimId : claimIds) {
            ReceiptClaim claim = receiptClaimRepository.findById(claimId).orElse(null);
            if (claim != null && ReceiptClaim.ClaimStatus.UPLOADED.equals(claim.getStatus())
                    && adminLocationAccessService.canAccessRestaurant(claim.getRestaurant().getId())) {
                claim.setStatus(ReceiptClaim.ClaimStatus.REJECTED);
                claim.setRejectionReason(reason);
                receiptClaimRepository.save(claim);
                rejected++;
            }
        }
        
        return Map.of(
            "totalRequested", claimIds.size(),
            "rejected", rejected
        );
    }

    @Override
    public Page<ReceiptClaimResponse> getByUser(Long userId, Pageable pageable) {
        log.info("Getting receipt claims by user - userId: {}", userId);
        List<Long> restaurantIds = adminLocationAccessService.getAccessibleRestaurantIds();
        Page<ReceiptClaim> claims = receiptClaimRepository.findByRestaurantIdInAndUserId(restaurantIds, userId, pageable);
        return claims.map(this::map);
    }

    @Override
    public Page<ReceiptClaimResponse> getByRestaurant(Long restaurantId, String status, Pageable pageable) {
        log.info("Getting receipt claims by restaurant - restaurantId: {}, status: {}", restaurantId, status);
        adminLocationAccessService.assertAccess(restaurantId);
        Page<ReceiptClaim> claims = receiptClaimRepository.findByRestaurantId(restaurantId, pageable);
        return claims.map(this::map);
    }

    @Override
    public Page<ReceiptClaimResponse> getDuplicateClaims(Long restaurantId, Long userId, Pageable pageable) {
        log.info("Getting duplicate receipt claims");
        List<Long> restaurantIds = adminLocationAccessService.resolveRestaurantIds(restaurantId);
        Page<ReceiptClaim> claims = receiptClaimRepository.findByRestaurantIdInAndStatus(restaurantIds, ReceiptClaim.ClaimStatus.DUPLICATE, pageable);
        return claims.map(this::map);
    }

    @Override
    public void exportReceiptClaimsCsv(Long restaurantId, String status, LocalDateTime from, LocalDateTime to, ByteArrayOutputStream out) {
        log.info("Exporting receipt claims to CSV");
        adminLocationAccessService.assertAccess(restaurantId);
        // Implementation for CSV export
    }

    @Override
    public Map<String, Object> getStatistics(Long restaurantId, LocalDateTime from, LocalDateTime to) {
        log.info("Getting receipt claim statistics");
        adminLocationAccessService.assertAccess(restaurantId);
        return Map.of(
            "totalClaims", 0L,
            "approved", 0L,
            "rejected", 0L,
            "pending", 0L,
            "duplicate", 0L,
            "totalPointsClaimed", 0L
        );
    }

    @Override
    public ReceiptClaimResponse markAsDuplicate(Long claimId, Long duplicateOfId) {
        log.info("Marking receipt claim as duplicate - claimId: {}", claimId);
        ReceiptClaim claim = receiptClaimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Receipt claim not found"));
        adminLocationAccessService.assertAccess(claim.getRestaurant().getId());

        claim.setStatus(ReceiptClaim.ClaimStatus.DUPLICATE);
        claim = receiptClaimRepository.save(claim);
        return map(claim);
    }

    @Override
    public ReceiptClaimResponse revertClaim(Long claimId) {
        log.info("Reverting receipt claim to pending - claimId: {}", claimId);
        ReceiptClaim claim = receiptClaimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Receipt claim not found"));
        adminLocationAccessService.assertAccess(claim.getRestaurant().getId());

        if (ReceiptClaim.ClaimStatus.APPROVED.equals(claim.getStatus()) ||
            ReceiptClaim.ClaimStatus.REJECTED.equals(claim.getStatus())) {
            claim.setStatus(ReceiptClaim.ClaimStatus.UPLOADED);
            claim.setApprovedAt(null);
            claim.setApprovedBy(null);
            claim.setRejectionReason(null);
            claim = receiptClaimRepository.save(claim);
        }
        
        return map(claim);
    }

    private ReceiptClaimResponse map(ReceiptClaim c) {
        return ReceiptClaimResponse.builder()
                .id(c.getId())
                .userId(c.getUserId())
                .fileUrl(c.getFileUrl())
                .status(c.getStatus() != null ? c.getStatus().name() : null)
                .pointsClaimed(c.getPointsClaimed())
                .approvedAt(c.getApprovedAt())
                .approvedBy(c.getApprovedBy() != null ? c.getApprovedBy().toString() : null)
                .rejectionReason(c.getRejectionReason())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
