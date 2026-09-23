package com.restaurant.waitlist.backend.controller;

import com.restaurant.waitlist.backend.dto.request.ClaimReceiptRequest;
import com.restaurant.waitlist.backend.dto.request.RedeemRewardRequest;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.ClaimReceiptResponse;
import com.restaurant.waitlist.backend.dto.response.GuestRewardsProfileResponse;
import com.restaurant.waitlist.backend.dto.response.RedeemRewardResponse;
import com.restaurant.waitlist.backend.service.CurrentUserResolver;
import com.restaurant.waitlist.backend.service.GuestRewardsService;
import com.restaurant.waitlist.backend.service.ReceiptClaimService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
@Slf4j
public class GuestRewardController {

    private final GuestRewardsService guestRewardsService;
    private final ReceiptClaimService receiptClaimService;
    private final CurrentUserResolver currentUserResolver;

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<GuestRewardsProfileResponse>> getProfile(
        @RequestParam(required = false) Long restaurantId
    ) {
        try {
            if (restaurantId == null) {
                restaurantId = 1L; // Default restaurant
            }

            Long userId = currentUserResolver.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("User not authenticated"));
            }

            GuestRewardsProfileResponse profile = guestRewardsService.getRewardsProfile(userId, restaurantId);
            return ResponseEntity.ok(ApiResponse.success("Rewards profile retrieved successfully", profile));
        } catch (Exception e) {
            log.error("Error fetching rewards profile", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{rewardId}/redeem")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<RedeemRewardResponse>> redeemReward(
        @PathVariable Long rewardId,
        @RequestParam Long restaurantId
    ) {
        try {
            Long userId = currentUserResolver.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("User not authenticated"));
            }

            RedeemRewardResponse response = guestRewardsService.redeemReward(userId, rewardId, restaurantId);
            return ResponseEntity.ok(ApiResponse.success("Reward redeemed successfully", response));
        } catch (Exception e) {
            log.error("Error redeeming reward", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/receipt/claim")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ClaimReceiptResponse>> claimReceipt(
        @RequestParam MultipartFile file,
        @RequestParam Long restaurantId,
        @RequestParam(required = false) String receiptAmount,
        @RequestParam(required = false) String receiptDate
    ) {
        try {
            Long userId = currentUserResolver.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("User not authenticated"));
            }

            ClaimReceiptResponse response = receiptClaimService.claimReceipt(userId, restaurantId, file, receiptAmount, receiptDate);
            return ResponseEntity.ok(ApiResponse.success("Receipt claimed successfully", response));
        } catch (IOException e) {
            log.error("Error processing receipt file", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Error processing file: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error claiming receipt", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
