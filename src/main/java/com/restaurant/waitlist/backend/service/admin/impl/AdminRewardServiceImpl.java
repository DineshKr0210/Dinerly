package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.request.admin.RewardSettingsRequest;
import com.restaurant.waitlist.backend.dto.request.admin.RewardTierRequest;
import com.restaurant.waitlist.backend.dto.response.admin.RewardTierResponse;
import com.restaurant.waitlist.backend.entity.RewardSettings;
import com.restaurant.waitlist.backend.entity.RewardTier;
import com.restaurant.waitlist.backend.repository.AuditLogRepository;
import com.restaurant.waitlist.backend.repository.RewardSettingsRepository;
import com.restaurant.waitlist.backend.repository.RewardTierRepository;
import com.restaurant.waitlist.backend.service.admin.AdminRewardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminRewardServiceImpl implements AdminRewardService {

    private final RewardTierRepository rewardTierRepository;
    private final RewardSettingsRepository rewardSettingsRepository;
    private final AuditLogRepository auditLogRepository;

    @Override
    public Page<RewardTierResponse> listTiers(Pageable pageable) {
        return rewardTierRepository.findAll(pageable).map(this::map);
    }

    @Override
    @Transactional
    public RewardTierResponse createTier(RewardTierRequest request) {
        RewardTier t = RewardTier.builder()
                .name(request.getName())
                .points(request.getPoints())
                .perks(request.getPerks())
                .build();
        RewardTier saved = rewardTierRepository.save(t);
        auditLogRepository.save(com.restaurant.waitlist.backend.entity.AuditLog.builder()
                .restaurantId(0L)
                .action("CREATE_REWARD_TIER")
                .details("Tier: " + saved.getName())
                .build());
        return map(saved);
    }

    @Override
    @Transactional
    public RewardTierResponse updateTier(Long tierId, RewardTierRequest request) {
        RewardTier t = rewardTierRepository.findById(tierId).orElseThrow(() -> new IllegalArgumentException("Tier not found"));
        t.setName(request.getName());
        t.setPoints(request.getPoints());
        t.setPerks(request.getPerks());
        RewardTier saved = rewardTierRepository.save(t);
        auditLogRepository.save(com.restaurant.waitlist.backend.entity.AuditLog.builder()
                .restaurantId(0L)
                .action("UPDATE_REWARD_TIER")
                .details("Tier: " + saved.getName())
                .build());
        return map(saved);
    }

    @Override
    @Transactional
    public void deleteTier(Long tierId) {
        rewardTierRepository.deleteById(tierId);
        auditLogRepository.save(com.restaurant.waitlist.backend.entity.AuditLog.builder()
                .restaurantId(0L)
                .action("DELETE_REWARD_TIER")
                .details("Tier deleted: " + tierId)
                .build());
    }

    @Override
    public List<RewardTierResponse> getAllTiers() {
        return rewardTierRepository.findAll().stream().map(this::map).collect(Collectors.toList());
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
        auditLogRepository.save(com.restaurant.waitlist.backend.entity.AuditLog.builder()
                .restaurantId(0L)
                .action("UPDATE_REWARD_SETTINGS")
                .details("Settings updated")
                .build());
        return request;
    }

    private RewardTierResponse map(RewardTier t) {
        return RewardTierResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .points(t.getPoints())
                .perks(t.getPerks())
                .build();
    }

    @Override
    public RewardTierResponse getTierById(Long tierId) {
        RewardTier tier = rewardTierRepository.findById(tierId)
            .orElseThrow(() -> new IllegalArgumentException("Tier not found"));
        return map(tier);
    }

    @Override
    public RewardTierResponse duplicateTier(Long tierId, String newName) {
        RewardTier original = rewardTierRepository.findById(tierId)
            .orElseThrow(() -> new IllegalArgumentException("Tier not found"));
        RewardTier duplicate = RewardTier.builder()
            .name(newName != null ? newName : original.getName() + " (Copy)")
            .points(original.getPoints())
            .perks(original.getPerks())
            .build();
        duplicate = rewardTierRepository.save(duplicate);
        return map(duplicate);
    }


    @Override
    public java.util.Map<String, Object> getStatistics(Long restaurantId) {
        return java.util.Map.of(
            "totalTiers", 0,
            "totalMembers", 0,
            "totalRedemptions", 0
        );
    }

    @Override
    public java.util.Map<String, Object> getUserTierDistribution(Long restaurantId) {
        return java.util.Map.of(
            "distribution", "data"
        );
    }
}
