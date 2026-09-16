package com.restaurant.waitlist.backend.service.impl;

import com.restaurant.waitlist.backend.dto.request.RedeemOfferRequest;
import com.restaurant.waitlist.backend.dto.response.GuestOfferResponse;
import com.restaurant.waitlist.backend.dto.response.RedeemOfferResponse;
import com.restaurant.waitlist.backend.entity.Offer;
import com.restaurant.waitlist.backend.entity.Redemption;
import com.restaurant.waitlist.backend.repository.OfferRepository;
import com.restaurant.waitlist.backend.repository.RedemptionRepository;
import com.restaurant.waitlist.backend.service.GuestOfferService;
import com.restaurant.waitlist.backend.service.PointsService;
import com.restaurant.waitlist.backend.util.RedemptionCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuestOfferServiceImpl implements GuestOfferService {

    private final OfferRepository offerRepository;
    private final RedemptionRepository redemptionRepository;
    private final PointsService pointsService;
    private static final int CODE_VALIDITY_MINUTES = 60; // 1 hour

    @Override
    public Page<GuestOfferResponse> getOffersByLocation(Long locationId, Long userId, Pageable pageable) {
        Page<Offer> offers = offerRepository.findActiveOffersByRestaurant(locationId, pageable);
        return offers.map(offer -> mapToGuestResponse(offer, userId));
    }

    @Override
    public Page<GuestOfferResponse> getOffersByLocationAndCategory(Long locationId, String category, Long userId, Pageable pageable) {
        Page<Offer> offers = offerRepository.findByRestaurantIdAndCategoryAndActive(locationId, category, pageable);
        return offers.map(offer -> mapToGuestResponse(offer, userId));
    }

    @Override
    public GuestOfferResponse getOfferDetail(Long offerId, Long userId) {
        Optional<Offer> offerOpt = offerRepository.findById(offerId);
        if (offerOpt.isEmpty()) {
            throw new IllegalArgumentException("Offer not found: " + offerId);
        }
        return mapToGuestResponse(offerOpt.get(), userId);
    }

    @Override
    @Transactional
    public RedeemOfferResponse redeemOffer(Long offerId, Long userId, Long restaurantId) {
        // Validate offer exists and is active
        Optional<Offer> offerOpt = offerRepository.findById(offerId);
        if (offerOpt.isEmpty()) {
            throw new IllegalArgumentException("Offer not found");
        }

        Offer offer = offerOpt.get();

        // Check if user can redeem
        if (!canUserRedeemOffer(userId, offerId)) {
            throw new IllegalArgumentException("User cannot redeem this offer: " + getRedemptionRestrictionReason(userId, offerId));
        }

        // Check inventory
        if (offer.getInventory() != null && offer.getInventory() <= 0) {
            throw new IllegalArgumentException("Offer is out of stock");
        }

        // Generate unique 6-digit code
        String code;
        int attempts = 0;
        do {
            code = RedemptionCodeGenerator.generate();
            attempts++;
            if (attempts > 10) {
                throw new RuntimeException("Failed to generate unique redemption code");
            }
        } while (redemptionRepository.existsByRedemptionCode(code));

        // Create redemption record
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(CODE_VALIDITY_MINUTES);
        Redemption redemption = Redemption.builder()
            .offer(offer)
            .restaurantId(restaurantId)
            .redemptionCode(code)
            .codeExpiresAt(expiresAt)
            .status(Redemption.RedemptionStatus.GENERATED)
            .userId(userId)
            .redeemedAt(LocalDateTime.now())
            .value(offer.getValue())
            .build();

        redemptionRepository.save(redemption);

        // Decrease inventory
        if (offer.getInventory() != null) {
            offer.setInventory(offer.getInventory() - 1);
            offerRepository.save(offer);
        }

        log.info("Redemption code generated for offer {} by user {}: {}", offerId, userId, code);

        return RedeemOfferResponse.builder()
            .redemptionId(redemption.getId())
            .redemptionCode(code)
            .expiresAt(expiresAt)
            .message("Show this code to your server")
            .offerName(offer.getName())
            .offerDescription(offer.getDescription())
            .build();
    }

    @Override
    @Transactional
    public void validateAndCompleteCode(String code) {
        Optional<Redemption> redemptionOpt = redemptionRepository.findValidRedemptionCode(code);
        if (redemptionOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid or expired redemption code");
        }

        Redemption redemption = redemptionOpt.get();
        redemption.setStatus(Redemption.RedemptionStatus.COMPLETED);
        redemptionRepository.save(redemption);

        log.info("Redemption code {} marked as completed", code);
    }

    @Override
    public boolean canUserRedeemOffer(Long userId, Long offerId) {
        String reason = getRedemptionRestrictionReason(userId, offerId);
        return reason == null;
    }

    @Override
    public String getRedemptionRestrictionReason(Long userId, Long offerId) {
        Optional<Offer> offerOpt = offerRepository.findById(offerId);
        if (offerOpt.isEmpty()) {
            return "Offer not found";
        }

        Offer offer = offerOpt.get();

        // Check if offer is active
        if (!"ACTIVE".equals(offer.getStatus())) {
            return "Offer is not active";
        }

        // Check expiry
        if (offer.getEndDate() != null && offer.getEndDate().isBefore(java.time.LocalDate.now())) {
            return "Offer has expired";
        }

        // Check per-user limit
        if (offer.getPerUserLimit() != null) {
            long totalRedemptions = offerRepository.countTotalRedemptionsByUserAndOffer(offerId, userId);
            if (totalRedemptions >= offer.getPerUserLimit()) {
                return "You have already redeemed this offer " + offer.getPerUserLimit() + " times";
            }
        }

        // Check per-user daily limit
        if (offer.getPerUserDailyLimit() != null) {
            long todayRedemptions = offerRepository.countTodayRedemptionsByUserAndOffer(offerId, userId);
            if (todayRedemptions >= offer.getPerUserDailyLimit()) {
                return "You have already redeemed this offer " + offer.getPerUserDailyLimit() + " times today";
            }
        }

        // Check inventory
        if (offer.getInventory() != null && offer.getInventory() <= 0) {
            return "Offer is out of stock";
        }

        // Check points requirement (if it's a points offer)
        if (offer.getPointsCost() != null && offer.getPointsCost() > 0) {
            long userPoints = pointsService.getBalance(userId);
            if (userPoints < offer.getPointsCost()) {
                return "Insufficient points. You need " + offer.getPointsCost() + " points but have " + userPoints;
            }
        }

        return null; // Can redeem
    }

    private GuestOfferResponse mapToGuestResponse(Offer offer, Long userId) {
        BigDecimal currentPrice = calculateCurrentPrice(offer);
        String reason = getRedemptionRestrictionReason(userId, offer.getId());
        boolean canRedeem = reason == null;

        long todayRedemptions = userId != null ? offerRepository.countTodayRedemptionsByUserAndOffer(offer.getId(), userId) : 0;
        long totalRedemptions = userId != null ? offerRepository.countTotalRedemptionsByUserAndOffer(offer.getId(), userId) : 0;

        return GuestOfferResponse.builder()
            .id(offer.getId())
            .name(offer.getName())
            .restaurantId(offer.getRestaurant().getId())
            .restaurantName(offer.getRestaurant().getName())
            .startDate(offer.getStartDate())
            .endDate(offer.getEndDate())
            .status(offer.getStatus())
            .discountType(offer.getDiscountType() != null ? offer.getDiscountType().toString() : null)
            .discountValue(offer.getDiscountValue())
            .discountLabel(offer.getDiscountLabel())
            .description(offer.getDescription())
            .restrictions(offer.getRestrictions())
            .photoUrl(offer.getPhotoUrl())
            .rating(offer.getRating())
            .ratingCount(offer.getRatingCount())
            .category(offer.getCategory())
            .originalPrice(offer.getOriginalPrice())
            .currentPrice(currentPrice)
            .redeemable(canRedeem)
            .reasonIfNotRedeemable(reason)
            .remainingInventory(offer.getInventory())
            .userRedemptionsToday((int) todayRedemptions)
            .userRedemptionsTotal((int) totalRedemptions)
            .value(offer.getValue())
            .pointsCost(offer.getPointsCost())
            .build();
    }

    private BigDecimal calculateCurrentPrice(Offer offer) {
        if (offer.getOriginalPrice() == null) {
            return offer.getValue();
        }

        if (offer.getDiscountType() == null || offer.getDiscountValue() == null) {
            return offer.getOriginalPrice();
        }

        switch (offer.getDiscountType()) {
            case PERCENT:
                return offer.getOriginalPrice().multiply(
                    BigDecimal.valueOf(100 - offer.getDiscountValue().doubleValue())
                        .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP)
                );
            case FIXED:
                return offer.getOriginalPrice().subtract(offer.getDiscountValue());
            default:
                return offer.getOriginalPrice();
        }
    }
}
