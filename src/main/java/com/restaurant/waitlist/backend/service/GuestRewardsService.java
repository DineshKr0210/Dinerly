package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.dto.response.GuestRewardsProfileResponse;
import com.restaurant.waitlist.backend.dto.response.RedeemRewardResponse;

public interface GuestRewardsService {
    /**
     * Get guest's complete rewards profile (points, tier, progress, redeemable items, ways to earn)
     */
    GuestRewardsProfileResponse getRewardsProfile(Long userId, Long restaurantId);

    /**
     * Redeem a reward item by deducting points
     */
    RedeemRewardResponse redeemReward(Long userId, Long rewardItemId, Long restaurantId);

    /**
     * Validate a generated reward redemption code and mark it complete.
     */
    java.util.Map<String, Object> validateRewardCode(String code);

    /**
     * Calculate current tier based on points
     */
    com.restaurant.waitlist.backend.entity.RewardTier calculateCurrentTier(Long userId, Long restaurantId);

    /**
     * Get next tier and progress towards it
     */
    GuestRewardsProfileResponse.TierProgress calculateTierProgress(Long userId, Long restaurantId);
}
