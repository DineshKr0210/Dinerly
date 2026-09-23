package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.request.admin.RewardSettingsRequest;
import com.restaurant.waitlist.backend.dto.request.admin.RewardTierRequest;
import com.restaurant.waitlist.backend.dto.response.admin.RewardTierResponse;
import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.entity.RewardSettings;
import com.restaurant.waitlist.backend.entity.RewardTier;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.RewardSettingsRepository;
import com.restaurant.waitlist.backend.repository.RewardTierRepository;
import com.restaurant.waitlist.backend.service.AdminLocationAccessService;
import com.restaurant.waitlist.backend.service.AuditLogService;
import com.restaurant.waitlist.backend.service.admin.AdminRewardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminRewardServiceImpl implements AdminRewardService {

    private final RewardTierRepository rewardTierRepository;
    private final RewardSettingsRepository rewardSettingsRepository;
    private final RestaurantRepository restaurantRepository;
    private final AdminLocationAccessService adminLocationAccessService;
    private final AuditLogService auditLogService;


    @Override
    public Page<RewardTierResponse> listTiers(Long restaurantId, Pageable pageable) {
        List<Long> restaurantIds = adminLocationAccessService.resolveRestaurantIds(restaurantId);
        return rewardTierRepository.findByRestaurantIdInOrderByTierOrderAsc(restaurantIds, pageable).map(this::map);
    }

    @Override
    @Transactional
    public RewardTierResponse createTier(RewardTierRequest request) {
        adminLocationAccessService.assertAccess(request.getRestaurantId());

        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + request.getRestaurantId()));

        RewardTier t = RewardTier.builder()
                .restaurant(restaurant)
                .name(request.getName())
                .pointsThreshold(request.getPointsThreshold())
                .tierOrder(request.getTierOrder())
                .color(request.getColor())
                .perks(request.getPerks())
                .build();
        RewardTier saved = rewardTierRepository.save(t);
        auditLogService.log(request.getRestaurantId(), "CREATE_REWARD_TIER", "Tier: " + saved.getName());
        return map(saved);
    }

    @Override
    @Transactional
    public RewardTierResponse updateTier(Long tierId, RewardTierRequest request) {
        RewardTier t = rewardTierRepository.findById(tierId).orElseThrow(() -> new IllegalArgumentException("Tier not found"));
        adminLocationAccessService.assertAccess(t.getRestaurant() != null ? t.getRestaurant().getId() : null);
        adminLocationAccessService.assertAccess(request.getRestaurantId());

        if (request.getRestaurantId() != null) {
            Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                    .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + request.getRestaurantId()));
            t.setRestaurant(restaurant);
        }
        
        t.setName(request.getName());
        t.setPointsThreshold(request.getPointsThreshold());
        t.setTierOrder(request.getTierOrder());
        t.setColor(request.getColor());
        t.setPerks(request.getPerks());
        
        RewardTier saved = rewardTierRepository.save(t);
        auditLogService.log(request.getRestaurantId(), "UPDATE_REWARD_TIER", "Tier: " + saved.getName());
        return map(saved);
    }

    @Override
    @Transactional
    public void deleteTier(Long tierId) {
        RewardTier tier = rewardTierRepository.findById(tierId)
                .orElseThrow(() -> new IllegalArgumentException("Tier not found"));
        Long restaurantId = tier.getRestaurant() != null ? tier.getRestaurant().getId() : 0L;
        adminLocationAccessService.assertAccess(tier.getRestaurant() != null ? tier.getRestaurant().getId() : null);
        rewardTierRepository.deleteById(tierId);
        auditLogService.log(restaurantId, "DELETE_REWARD_TIER", "Tier deleted: " + tier.getName() + " (ID: " + tierId + ")");
    }

    @Override
    public RewardSettingsRequest getSettings() {
        RewardSettings s = rewardSettingsRepository.findTopByOrderByIdDesc().orElse(RewardSettings.builder().preventDuplicateRedemptionsWithinVisit(true).build());
        RewardSettingsRequest r = new RewardSettingsRequest();
        r.setPreventDuplicateRedemptionsWithinVisit(s.getPreventDuplicateRedemptionsWithinVisit());
        return r;
    }

    @Override
    @Transactional
    public RewardSettingsRequest updateSettings(RewardSettingsRequest request) {
        RewardSettings s = RewardSettings.builder().preventDuplicateRedemptionsWithinVisit(request.getPreventDuplicateRedemptionsWithinVisit()).build();
        rewardSettingsRepository.save(s);
        auditLogService.log(0L, "UPDATE_REWARD_SETTINGS", "Settings updated");
        return request;
    }

    private RewardTierResponse map(RewardTier t) {
        return RewardTierResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .pointsThreshold(t.getPointsThreshold())
                .tierOrder(t.getTierOrder())
                .color(t.getColor())
                .perks(t.getPerks())
                .restaurantId(t.getRestaurant() != null ? t.getRestaurant().getId() : null)
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public RewardTierResponse duplicateTier(Long tierId, String newName) {
        RewardTier original = rewardTierRepository.findById(tierId)
            .orElseThrow(() -> new IllegalArgumentException("Tier not found"));
        adminLocationAccessService.assertAccess(original.getRestaurant() != null ? original.getRestaurant().getId() : null);
        RewardTier duplicate = RewardTier.builder()
            .restaurant(original.getRestaurant())
            .name(newName != null ? newName : original.getName() + " (Copy)")
            .pointsThreshold(original.getPointsThreshold())
            .tierOrder(original.getTierOrder())
            .color(original.getColor())
            .perks(original.getPerks())
            .build();
        duplicate = rewardTierRepository.save(duplicate);
        Long restaurantId = duplicate.getRestaurant() != null ? duplicate.getRestaurant().getId() : 0L;
        auditLogService.log(restaurantId, "DUPLICATE_REWARD_TIER", "Tier duplicated: " + original.getName() + " -> " + duplicate.getName());
        return map(duplicate);
    }


    @Override
    public java.util.Map<String, Object> getStatistics(Long restaurantId) {
        List<Long> restaurantIds = adminLocationAccessService.resolveRestaurantIds(restaurantId);
        long totalTiers = rewardTierRepository.findByRestaurantIdIn(restaurantIds).size();
        return java.util.Map.of(
            "totalTiers", totalTiers,
            "totalMembers", 0,
            "totalRedemptions", 0
        );
    }

    @Override
    public java.util.Map<String, Object> getUserTierDistribution(Long restaurantId) {
        List<Long> restaurantIds = adminLocationAccessService.resolveRestaurantIds(restaurantId);
        long tierCount = rewardTierRepository.findByRestaurantIdIn(restaurantIds).size();
        return java.util.Map.of("tierCount", tierCount, "restaurantId", restaurantId != null ? restaurantId : "all");
    }
}
