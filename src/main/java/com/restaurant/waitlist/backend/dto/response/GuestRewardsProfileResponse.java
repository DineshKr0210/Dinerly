package com.restaurant.waitlist.backend.dto.response;

import com.restaurant.waitlist.backend.dto.response.admin.RewardItemResponse;
import com.restaurant.waitlist.backend.dto.response.admin.PointsEarningRuleResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GuestRewardsProfileResponse {
    private Long currentPoints;
    private TierInfo currentTier;
    private TierProgress tierProgress;
    private List<RedeemableRewardItem> redeemableRewards;
    private List<WayToEarn> waysToEarn;

    @Data
    @Builder
    public static class TierInfo {
        private Long id;
        private String name;
        private String color;
        private Long pointsThreshold;
        private Integer tierOrder;
    }

    @Data
    @Builder
    public static class TierProgress {
        private Long pointsToNextTier;
        private String nextTierName;
        private Double progressPercentage;
        private Integer nextTierOrder;
    }

    @Data
    @Builder
    public static class RedeemableRewardItem {
        private Long id;
        private String title;
        private String description;
        private Long pointsCost;
        private String icon;
        private String status; // REDEEMABLE, NOT_ENOUGH_POINTS, UNAVAILABLE
    }

    @Data
    @Builder
    public static class WayToEarn {
        private String action;
        private String title;
        private String subtitle;
        private Long pointsValue;
        private Boolean clickable;
        private String actionUrl;
        private String icon;
    }
}
