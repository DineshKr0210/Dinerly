package com.restaurant.waitlist.backend.service.impl;

import com.restaurant.waitlist.backend.dto.response.ClaimReceiptResponse;
import com.restaurant.waitlist.backend.entity.ReceiptClaim;
import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.repository.ReceiptClaimRepository;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.service.PointsService;
import com.restaurant.waitlist.backend.service.ReceiptClaimService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReceiptClaimServiceImpl implements ReceiptClaimService {

    private final ReceiptClaimRepository receiptClaimRepository;
    private final RestaurantRepository restaurantRepository;
    private final PointsService pointsService;

    @Value("${upload.receipts.path:/tmp/receipts}")
    private String uploadPath;

    @Value("${receipt.points:15}")
    private Long defaultReceiptPoints;

    @Value("${receipt.auto-approve:false}")
    private boolean autoApproveReceipts;

    @Override
    @Transactional
    public ClaimReceiptResponse claimReceipt(Long userId, Long restaurantId, MultipartFile file, String receiptAmount, String receiptDate) throws IOException {
        // Validate restaurant
        Optional<Restaurant> restaurantOpt = restaurantRepository.findById(restaurantId);
        if (restaurantOpt.isEmpty()) {
            throw new IllegalArgumentException("Restaurant not found");
        }

        // Check for duplicates
        if (isDuplicateReceipt(userId, restaurantId, receiptAmount, receiptDate)) {
            throw new IllegalArgumentException("Duplicate receipt detected. This receipt has already been claimed.");
        }

        // Validate file
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        if (!isValidReceiptFile(file)) {
            throw new IllegalArgumentException("Invalid file type. Only images (JPEG, PNG) are allowed.");
        }

        // Upload file
        String fileUrl = uploadReceiptFile(file);

        // Get points for this receipt
        Long pointsToClaim = calculatePointsForReceipt(restaurantId);

        // Create receipt claim
        ReceiptClaim claim = ReceiptClaim.builder()
            .userId(userId)
            .restaurant(restaurantOpt.get())
            .fileUrl(fileUrl)
            .receiptAmount(receiptAmount)
            .receiptDate(receiptDate)
            .pointsClaimed(pointsToClaim)
            .status(autoApproveReceipts ? ReceiptClaim.ClaimStatus.APPROVED : ReceiptClaim.ClaimStatus.UPLOADED)
            .build();

        receiptClaimRepository.save(claim);

        // Auto-approve and credit points if enabled
        Long newBalance = pointsService.getBalance(userId);
        if (autoApproveReceipts) {
            pointsService.credit(userId, pointsToClaim, "Receipt claim approved: " + fileUrl, "receipt_claim", claim.getId());
            claim.setApprovedAt(LocalDateTime.now());
            receiptClaimRepository.save(claim);
            newBalance = pointsService.getBalance(userId);
        }

        log.info("Receipt claimed by user {} from restaurant {}: {} points", userId, restaurantId, pointsToClaim);

        return ClaimReceiptResponse.builder()
            .pointsClaimed(pointsToClaim)
            .newBalance(newBalance)
            .receiptReference("RCP-" + LocalDate.now() + "-" + claim.getId())
            .status(claim.getStatus().toString())
            .message(autoApproveReceipts ? "Points added to your account!" : "Receipt submitted for approval")
            .claimedAt(claim.getCreatedAt())
            .build();
    }

    @Override
    public List<ReceiptClaim> getPendingReceipts(Long restaurantId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return receiptClaimRepository.findByRestaurantIdAndStatus(restaurantId, ReceiptClaim.ClaimStatus.UPLOADED, pageable)
            .getContent();
    }

    @Override
    @Transactional
    public void approveReceiptClaim(Long receiptClaimId, Long adminId) {
        Optional<ReceiptClaim> claimOpt = receiptClaimRepository.findById(receiptClaimId);
        if (claimOpt.isEmpty()) {
            throw new IllegalArgumentException("Receipt claim not found");
        }

        ReceiptClaim claim = claimOpt.get();

        if (claim.getStatus() != ReceiptClaim.ClaimStatus.UPLOADED) {
            throw new IllegalArgumentException("Only pending receipts can be approved");
        }

        // Credit points
        pointsService.credit(claim.getUserId(), claim.getPointsClaimed(), 
            "Receipt approved: " + claim.getFileUrl(), "receipt_claim", receiptClaimId);

        // Update receipt
        claim.setStatus(ReceiptClaim.ClaimStatus.APPROVED);
        claim.setApprovedAt(LocalDateTime.now());
        claim.setApprovedBy(adminId);
        receiptClaimRepository.save(claim);

        log.info("Receipt claim {} approved by admin {}", receiptClaimId, adminId);
    }

    @Override
    @Transactional
    public void rejectReceiptClaim(Long receiptClaimId, Long adminId, String reason) {
        Optional<ReceiptClaim> claimOpt = receiptClaimRepository.findById(receiptClaimId);
        if (claimOpt.isEmpty()) {
            throw new IllegalArgumentException("Receipt claim not found");
        }

        ReceiptClaim claim = claimOpt.get();

        if (claim.getStatus() != ReceiptClaim.ClaimStatus.UPLOADED) {
            throw new IllegalArgumentException("Only pending receipts can be rejected");
        }

        // Update receipt
        claim.setStatus(ReceiptClaim.ClaimStatus.REJECTED);
        claim.setRejectionReason(reason);
        claim.setApprovedAt(LocalDateTime.now());
        claim.setApprovedBy(adminId);
        receiptClaimRepository.save(claim);

        log.info("Receipt claim {} rejected by admin {}: {}", receiptClaimId, adminId, reason);
    }

    @Override
    public boolean isDuplicateReceipt(Long userId, Long restaurantId, String amount, String date) {
        if (amount == null || date == null) {
            return false;
        }

        // Check for identical receipt in last 24 hours
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<ReceiptClaim> claims = receiptClaimRepository.findByUserIdAndRestaurantIdAndCreatedAtAfter(userId, restaurantId, since);

        for (ReceiptClaim claim : claims) {
            if (amount.equals(claim.getReceiptAmount()) && date.equals(claim.getReceiptDate())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Long calculatePointsForReceipt(Long restaurantId) {
        // TODO: Add restaurant-specific configuration
        return defaultReceiptPoints;
    }

    private String uploadReceiptFile(MultipartFile file) throws IOException {
        // Create uploads directory if it doesn't exist
        File uploadsDir = new File(uploadPath);
        if (!uploadsDir.exists()) {
            uploadsDir.mkdirs();
        }

        // Generate unique filename
        String filename = "receipt-" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        File uploadedFile = new File(uploadsDir, filename);

        // Save file
        file.transferTo(uploadedFile);

        log.info("Receipt file uploaded: {}", filename);

        return uploadPath + "/" + filename;
    }

    private boolean isValidReceiptFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        return contentType.equals("image/jpeg") || 
               contentType.equals("image/png") || 
               contentType.equals("image/jpg");
    }
}
