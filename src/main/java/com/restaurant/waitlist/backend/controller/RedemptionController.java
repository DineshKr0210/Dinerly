package com.restaurant.waitlist.backend.controller;

import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.service.GuestOfferService;
import com.restaurant.waitlist.backend.service.GuestRewardsService;
import com.restaurant.waitlist.backend.service.admin.AdminRedemptionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
            com.restaurant.waitlist.backend.dto.response.ConfirmRedemptionResponse response = guestOfferService.validateAndCompleteCode(code);
            return ResponseEntity.ok(ApiResponse.success("Offer code validated and completed", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid or expired offer code: " + e.getMessage()));
        }
    }

    /**
     * Validate and complete a campaign redemption code at POS (with campaign ID)
     * Used when guest enters code at restaurant to redeem campaign offer
     * Accessible by Host, Manager, or Staff roles
     *
     * @param restaurantId The restaurant ID where POS is validating the code
     * @param code The 6-digit redemption code entered by guest
     * @param campaignId The campaign ID associated with the code
     * @return Success response with redemption details or error if code invalid/expired
     */

    /**
     * Validate and complete a campaign redemption code at POS (code only)
     * Backend automatically looks up which campaign the code belongs to
     * Simpler endpoint when you only have the redemption code
     * Accessible by Host, Manager, or Staff roles
     *
     * @param restaurantId The restaurant ID where POS is validating the code
     * @param code The 6-digit redemption code entered by guest
     * @return Success response with redemption details or error if code invalid/expired
     */
    @PostMapping("/validate-campaign-code/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'HOST')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateCampaignCodeByCodeOnly(
            @PathVariable Long restaurantId,
            @PathVariable String code) {
        
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Code is required"));
        }
        
        Map<String, Object> resp = adminRedemptionService.validateAndCompleteCampaignCodeByCode(code);
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

        Map<String, Object> resp = guestRewardsService.validateRewardCode(code);
        return ResponseEntity.ok(ApiResponse.success("Reward code validated successfully", resp));
    }
}

