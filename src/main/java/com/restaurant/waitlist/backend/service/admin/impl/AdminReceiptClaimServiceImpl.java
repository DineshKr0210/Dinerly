package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.request.admin.ApproveReceiptRequest;
import com.restaurant.waitlist.backend.dto.request.admin.RejectReceiptRequest;
import com.restaurant.waitlist.backend.dto.response.admin.ReceiptClaimResponse;
import com.restaurant.waitlist.backend.entity.ReceiptClaim;
import com.restaurant.waitlist.backend.repository.ReceiptClaimRepository;
import com.restaurant.waitlist.backend.service.admin.AdminReceiptClaimService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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
    private final ModelMapper modelMapper;

    @Override
    public Page<ReceiptClaimResponse> listReceiptClaims(Long restaurantId, String status, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        log.info("Listing receipt claims - restaurantId: {}, status: {}", restaurantId, status);
        Page<ReceiptClaim> claims = receiptClaimRepository.findAll(pageable);
        return claims.map(claim -> modelMapper.map(claim, ReceiptClaimResponse.class));
    }

    @Override
    public Page<ReceiptClaimResponse> listPendingClaims(Long restaurantId, Pageable pageable) {
        log.info("Listing pending receipt claims - restaurantId: {}", restaurantId);
        Page<ReceiptClaim> claims = receiptClaimRepository.findByRestaurantIdAndStatus(restaurantId, "UPLOADED", pageable);
        return claims.map(claim -> modelMapper.map(claim, ReceiptClaimResponse.class));
    }

    @Override
    public ReceiptClaimResponse getReceiptClaimById(Long claimId) {
        log.info("Getting receipt claim - claimId: {}", claimId);
        ReceiptClaim claim = receiptClaimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Receipt claim not found"));
        return modelMapper.map(claim, ReceiptClaimResponse.class);
    }

    @Override
    public ReceiptClaimResponse getReceiptClaimDetails(Long claimId) {
        log.info("Getting receipt claim details - claimId: {}", claimId);
        ReceiptClaim claim = receiptClaimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Receipt claim not found"));
        return modelMapper.map(claim, ReceiptClaimResponse.class);
    }

    @Override
    public ReceiptClaimResponse approveReceiptClaim(Long claimId, ApproveReceiptRequest request) {
        log.info("Approving receipt claim - claimId: {}, points: {}", claimId, request.getPointsOverride());
        ReceiptClaim claim = receiptClaimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Receipt claim not found"));
        
        claim.setStatus("APPROVED");
        claim.setPointsClaimed(request.getPointsOverride());
        claim.setApprovedAt(LocalDateTime.now());
        claim.setApprovedBy("admin");
        
        claim = receiptClaimRepository.save(claim);
        return modelMapper.map(claim, ReceiptClaimResponse.class);
    }

    @Override
    public ReceiptClaimResponse rejectReceiptClaim(Long claimId, RejectReceiptRequest request) {
        log.info("Rejecting receipt claim - claimId: {}, reason: {}", claimId, request.getReason());
        ReceiptClaim claim = receiptClaimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Receipt claim not found"));
        
        claim.setStatus("REJECTED");
        claim.setRejectionReason(request.getReason());
        
        claim = receiptClaimRepository.save(claim);
        return modelMapper.map(claim, ReceiptClaimResponse.class);
    }

    @Override
    public Map<String, Object> approveBulkClaims(List<Long> claimIds, Long pointsOverride) {
        log.info("Approving {} receipt claims in bulk", claimIds.size());
        int approved = 0;
        
        for (Long claimId : claimIds) {
            ReceiptClaim claim = receiptClaimRepository.findById(claimId).orElse(null);
            if (claim != null && "UPLOADED".equals(claim.getStatus())) {
                claim.setStatus("APPROVED");
                if (pointsOverride != null) {
                    claim.setPointsClaimed(pointsOverride);
                }
                claim.setApprovedAt(LocalDateTime.now());
                claim.setApprovedBy("admin");
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
            if (claim != null && "UPLOADED".equals(claim.getStatus())) {
                claim.setStatus("REJECTED");
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
        Page<ReceiptClaim> claims = receiptClaimRepository.findByUserId(userId, pageable);
        return claims.map(claim -> modelMapper.map(claim, ReceiptClaimResponse.class));
    }

    @Override
    public Page<ReceiptClaimResponse> getByRestaurant(Long restaurantId, String status, Pageable pageable) {
        log.info("Getting receipt claims by restaurant - restaurantId: {}, status: {}", restaurantId, status);
        Page<ReceiptClaim> claims = receiptClaimRepository.findByRestaurantId(restaurantId, pageable);
        return claims.map(claim -> modelMapper.map(claim, ReceiptClaimResponse.class));
    }

    @Override
    public Page<ReceiptClaimResponse> getDuplicateClaims(Long restaurantId, Long userId, Pageable pageable) {
        log.info("Getting duplicate receipt claims");
        Page<ReceiptClaim> claims = receiptClaimRepository.findByStatus("DUPLICATE", pageable);
        return claims.map(claim -> modelMapper.map(claim, ReceiptClaimResponse.class));
    }

    @Override
    public void exportReceiptClaimsCsv(Long restaurantId, String status, LocalDateTime from, LocalDateTime to, ByteArrayOutputStream out) {
        log.info("Exporting receipt claims to CSV");
        // Implementation for CSV export
    }

    @Override
    public Map<String, Object> getStatistics(Long restaurantId, LocalDateTime from, LocalDateTime to) {
        log.info("Getting receipt claim statistics");
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
        
        claim.setStatus("DUPLICATE");
        claim = receiptClaimRepository.save(claim);
        return modelMapper.map(claim, ReceiptClaimResponse.class);
    }

    @Override
    public ReceiptClaimResponse revertClaim(Long claimId) {
        log.info("Reverting receipt claim to pending - claimId: {}", claimId);
        ReceiptClaim claim = receiptClaimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Receipt claim not found"));
        
        if ("APPROVED".equals(claim.getStatus()) || "REJECTED".equals(claim.getStatus())) {
            claim.setStatus("UPLOADED");
            claim.setApprovedAt(null);
            claim.setApprovedBy(null);
            claim.setRejectionReason(null);
            claim = receiptClaimRepository.save(claim);
        }
        
        return modelMapper.map(claim, ReceiptClaimResponse.class);
    }
}
