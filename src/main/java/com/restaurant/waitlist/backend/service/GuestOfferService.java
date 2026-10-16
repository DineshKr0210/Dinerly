package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.dto.request.RedeemOfferRequest;
import com.restaurant.waitlist.backend.dto.request.ValidateOfferRequest;
import com.restaurant.waitlist.backend.dto.response.GuestOfferResponse;
import com.restaurant.waitlist.backend.dto.response.RedeemOfferResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GuestOfferService {
    /**
     * Get all active offers for a restaurant
     */
    Page<GuestOfferResponse> getOffersByLocation(Long locationId, Long userId, Pageable pageable);

    /**
     * Get offers filtered by category
     */
    Page<GuestOfferResponse> getOffersByLocationAndCategory(Long locationId, String category, Long userId, Pageable pageable);

    /**
     * Get a single offer detail
     */
    GuestOfferResponse getOfferDetail(Long offerId, Long userId);

    /**
     * Redeem an offer and generate 6-digit code
     */
    RedeemOfferResponse redeemOffer(Long offerId, Long userId, Long restaurantId);

    /**
     * Validate redemption code and mark as completed.
     * The code must belong to the given restaurant — a code generated at one
     * location cannot be redeemed at another.
     */
    com.restaurant.waitlist.backend.dto.response.ConfirmRedemptionResponse validateAndCompleteCode(String code, Long restaurantId);

    /**
     * Validate offer redemption using QR code scan OR manual code entry (NEW).
     * Supports both QR scanning and keyboard entry at POS.
     *
     * @param validateRequest Contains either redemptionCode (manual) or scannedQRText (QR scan)
     * @param restaurantId The restaurant validating the offer
     * @return ConfirmRedemptionResponse with offer details
     */
    com.restaurant.waitlist.backend.dto.response.ConfirmRedemptionResponse validateOfferByCodeOrQR(
            ValidateOfferRequest validateRequest, Long restaurantId);

    /**
     * Check if user can redeem an offer
     */
    boolean canUserRedeemOffer(Long userId, Long offerId);

    /**
     * Get reason if user cannot redeem
     */
    String getRedemptionRestrictionReason(Long userId, Long offerId);
}
