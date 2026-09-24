package com.restaurant.waitlist.backend.controller;

import com.restaurant.waitlist.backend.dto.request.ValidateCampaignCodeRequest;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.service.GuestOfferService;
import com.restaurant.waitlist.backend.service.GuestRewardsService;
import com.restaurant.waitlist.backend.service.admin.AdminRedemptionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/restaurants/{restaurantId}/redemptions")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Restaurant Redemptions", description = "POS-side validation of offer, campaign, and reward codes")
public class RedemptionController {

    private final AdminRedemptionService adminRedemptionService;
    private final GuestOfferService guestOfferService;
    private final GuestRewardsService guestRewardsService;

    /**
     * Validate and complete an offer redemption code at POS
     * Used when guest/staff enters offer code at restaurant
     * Accessible by Host, Manager, or Staff roles
     *
     * @param restaurantId The restaurant ID where POS is validating the code
     * @param code The 6-digit offer redemption code
     * @return Success response with offer details or error if code invalid/expired
     */
    @PostMapping("/validate-offer-code/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'HOST')")
    public ResponseEntity<ApiResponse<com.restaurant.waitlist.backend.dto.response.ConfirmRedemptionResponse>> validateOfferCode(
            @PathVariable Long restaurantId,
            @PathVariable String code) {
        try {
            com.restaurant.waitlist.backend.dto.response.ConfirmRedemptionResponse response = guestOfferService.validateAndCompleteCode(code, restaurantId);
            return ResponseEntity.ok(ApiResponse.success("Offer code validated and completed", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid or expired offer code: " + e.getMessage()));
        }
    }

    /**
     * Validate and complete a campaign's shared coupon code at POS.
     * Every recipient of a campaign gets the same code (e.g. "BROTPIZZA50"), so the
     * guest's phone number is required to look up which campaign it belongs to and
     * to make sure the same guest can't redeem it twice.
     * Accessible by Host, Manager, or Staff roles.
     *
     * @param restaurantId The restaurant ID where POS is validating the code
     * @param code The shared campaign coupon code entered by guest
     * @param request Contains the guest's phone number
     * @return Success response with redemption details or error if code invalid/expired/already used
     */
    @PostMapping("/validate-campaign-code/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'HOST')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateCampaignCodeByCodeOnly(
            @PathVariable Long restaurantId,
            @PathVariable String code,
            @RequestBody ValidateCampaignCodeRequest request) {

        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Code is required"));
        }
        if (request == null || request.getGuestPhone() == null || request.getGuestPhone().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Guest phone is required"));
        }

        Map<String, Object> resp = adminRedemptionService.validateAndCompleteCampaignCodeByCode(code, restaurantId, request.getGuestPhone());
        return ResponseEntity.ok(ApiResponse.success("Campaign code validated successfully", resp));
    }

    /**
     * Validate and complete a reward redemption code at POS.
     */
    @PostMapping("/validate-reward-code/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'HOST')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateRewardCode(
            @PathVariable Long restaurantId,
            @PathVariable String code) {
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Code is required"));
        }

        Map<String, Object> resp = guestRewardsService.validateRewardCode(code, restaurantId);
        return ResponseEntity.ok(ApiResponse.success("Reward code validated successfully", resp));
    }
}

