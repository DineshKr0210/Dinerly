package com.restaurant.waitlist.backend.service.impl;

import com.restaurant.waitlist.backend.dto.response.GuestRewardsProfileResponse;
import com.restaurant.waitlist.backend.dto.response.RedeemRewardResponse;
import com.restaurant.waitlist.backend.entity.*;
import com.restaurant.waitlist.backend.repository.*;
import com.restaurant.waitlist.backend.service.GuestRewardsService;
import com.restaurant.waitlist.backend.service.PointsService;
import com.restaurant.waitlist.backend.util.RedemptionCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuestRewardsServiceImpl implements GuestRewardsService {

    private final PointsService pointsService;
    private final RewardTierRepository rewardTierRepository;
    private final RewardItemRepository rewardItemRepository;
    private final PointsEarningRuleRepository pointsEarningRuleRepository;
    private final RedemptionRepository redemptionRepository;
    private final DinerlyPointsRepository dinerlyPointsRepository;
    private static final int CODE_VALIDITY_MINUTES = 60; // 1 hour

    @Override
    public GuestRewardsProfileResponse getRewardsProfile(Long userId, Long restaurantId) {
        // Get user's current points
        long currentPoints = pointsService.getBalance(userId);

        // Calculate current tier
        RewardTier currentTier = calculateCurrentTier(userId, restaurantId);

        // Calculate tier progress
        GuestRewardsProfileResponse.TierProgress tierProgress = calculateTierProgress(userId, restaurantId);

        // Get redeemable rewards
        List<GuestRewardsProfileResponse.RedeemableRewardItem> redeemableRewards = getRedeemableRewards(userId, restaurantId, currentPoints);

        // Get ways to earn
        List<GuestRewardsProfileResponse.WayToEarn> waysToEarn = getWaysToEarn(restaurantId);

        return GuestRewardsProfileResponse.builder()
            .currentPoints(currentPoints)
            .currentTier(mapTierToTierInfo(currentTier))
            .tierProgress(tierProgress)
            .redeemableRewards(redeemableRewards)
            .waysToEarn(waysToEarn)
            .build();
    }

    @Override
    @Transactional
    public RedeemRewardResponse redeemReward(Long userId, Long rewardItemId, Long restaurantId) {
        // Get reward item
        Optional<RewardItem> rewardOpt = rewardItemRepository.findById(rewardItemId);
        if (rewardOpt.isEmpty()) {
            throw new IllegalArgumentException("Reward item not found");
        }

        RewardItem reward = rewardOpt.get();

        // Check if item is available
        if (!reward.getAvailable()) {
            throw new IllegalArgumentException("Reward is not available");
        }

        // Get user's points
        long userPoints = pointsService.getBalance(userId);

        // Check if user has enough points
        if (userPoints < reward.getPointsCost()) {
            throw new IllegalArgumentException("Insufficient points. Need " + reward.getPointsCost() + " but have " + userPoints);
        }

        // Generate 6-digit code
        String code;
        int attempts = 0;
        do {
            code = RedemptionCodeGenerator.generate();
            attempts++;
            if (attempts > 10) {
                throw new RuntimeException("Failed to generate unique code");
            }
        } while (redemptionRepository.existsByRedemptionCode(code));

        // Deduct points using PointsService
        pointsService.debit(userId, reward.getPointsCost(), "Reward redemption: " + reward.getTitle(), "reward_redemption", rewardItemId);

        // Create redemption record (reuse Redemption entity or create new table for rewards)
        // For now, we'll create a pseudo-offer redemption with reward info
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(CODE_VALIDITY_MINUTES);

        // Log redemption
        log.info("User {} redeemed reward {} for {} points", userId, rewardItemId, reward.getPointsCost());

        return RedeemRewardResponse.builder()
            .redemptionCode(code)
            .expiresAt(expiresAt)
            .message("Your reward code: " + code)
            .newPointsBalance(userPoints - reward.getPointsCost())
            .rewardTitle(reward.getTitle())
            .build();
    }

    @Override
    public RewardTier calculateCurrentTier(Long userId, Long restaurantId) {
        long userPoints = pointsService.getBalance(userId);

        // Find the highest tier the user actually qualifies for.
        // If the user is below the first tier threshold, there is no current tier yet.
        return rewardTierRepository.findTierForPoints(restaurantId, userPoints)
            .orElse(null);
    }

    @Override
    public GuestRewardsProfileResponse.TierProgress calculateTierProgress(Long userId, Long restaurantId) {
        long userPoints = pointsService.getBalance(userId);

        List<RewardTier> tiers = rewardTierRepository.findByRestaurantIdOrderByTierOrderAsc(restaurantId);
        if (tiers.isEmpty()) {
            return null;
        }

        // Find current and next tier
        RewardTier currentTier = null;
        RewardTier nextTier = null;

        for (RewardTier tier : tiers) {
            if (userPoints >= tier.getPointsThreshold()) {
                currentTier = tier;
            } else if (nextTier == null) {
                nextTier = tier;
                break;
            }
        }

        if (nextTier == null) {
            // User is at highest tier
            return GuestRewardsProfileResponse.TierProgress.builder()
                .pointsToNextTier(0L)
                .nextTierName("Maximum")
                .progressPercentage(100.0)
                .nextTierOrder(null)
                .build();
        }

        long pointsToNextTier = nextTier.getPointsThreshold() - userPoints;

        if (currentTier == null) {
            return GuestRewardsProfileResponse.TierProgress.builder()
                .pointsToNextTier(Math.max(pointsToNextTier, 0L))
                .nextTierName(nextTier.getName())
                .progressPercentage(0.0)
                .nextTierOrder(nextTier.getTierOrder())
                .build();
        }

        long currentTierThreshold = currentTier.getPointsThreshold();
        long nextTierThreshold = nextTier.getPointsThreshold();
        if (nextTierThreshold <= currentTierThreshold) {
            return GuestRewardsProfileResponse.TierProgress.builder()
                .pointsToNextTier(Math.max(pointsToNextTier, 0L))
                .nextTierName(nextTier.getName())
                .progressPercentage(0.0)
                .nextTierOrder(nextTier.getTierOrder())
                .build();
        }

        double progressPercentage = (double) (userPoints - currentTierThreshold) /
            (nextTierThreshold - currentTierThreshold) * 100;

        return GuestRewardsProfileResponse.TierProgress.builder()
            .pointsToNextTier(Math.max(pointsToNextTier, 0L))
            .nextTierName(nextTier.getName())
            .progressPercentage(Math.min(progressPercentage, 100.0))
            .nextTierOrder(nextTier.getTierOrder())
            .build();
    }

    private List<GuestRewardsProfileResponse.RedeemableRewardItem> getRedeemableRewards(Long userId, Long restaurantId, long userPoints) {
        List<RewardItem> rewards = rewardItemRepository.findByRestaurantIdAndAvailableTrue(restaurantId);

        return rewards.stream()
            .map(reward -> {
                String status;
                if (userPoints < reward.getPointsCost()) {
                    status = "NOT_ENOUGH_POINTS";
                } else {
                    status = "REDEEMABLE";
                }

                return GuestRewardsProfileResponse.RedeemableRewardItem.builder()
                    .id(reward.getId())
                    .title(reward.getTitle())
                    .description(reward.getDescription())
                    .pointsCost(reward.getPointsCost())
                    .icon(reward.getIcon())
                    .status(status)
                    .build();
            })
            .collect(Collectors.toList());
    }

    private List<GuestRewardsProfileResponse.WayToEarn> getWaysToEarn(Long restaurantId) {
        List<PointsEarningRule> rules = pointsEarningRuleRepository.findByRestaurantId(restaurantId);

        return rules.stream()
            .map(rule -> GuestRewardsProfileResponse.WayToEarn.builder()
                .action(rule.getAction())
                .title(getActionTitle(rule.getAction()))
                .subtitle(rule.getDescription())
                .pointsValue(rule.getPointsValue())
                .clickable(rule.getClickable() != null ? rule.getClickable() : false)
                .actionUrl(rule.getActionUrl())
                .icon(rule.getIcon())
                .build())
            .collect(Collectors.toList());
    }

    private String getActionTitle(String action) {
        return switch (action) {
            case "dine_in" -> "Dined without joining waitlist?";
            case "join_waitlist" -> "Join the waitlist";
            case "leave_review" -> "Leave a review";
            case "refer_friend" -> "Refer a friend";
            default -> action;
        };
    }

    private GuestRewardsProfileResponse.TierInfo mapTierToTierInfo(RewardTier tier) {
        if (tier == null) {
            return null;
        }

        return GuestRewardsProfileResponse.TierInfo.builder()
            .id(tier.getId())
            .name(tier.getName())
            .color(tier.getColor())
            .pointsThreshold(tier.getPointsThreshold())
            .tierOrder(tier.getTierOrder())
            .build();
    }
}
