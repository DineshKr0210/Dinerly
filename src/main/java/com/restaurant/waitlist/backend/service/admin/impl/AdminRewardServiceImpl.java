package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.request.admin.RewardSettingsRequest;
import com.restaurant.waitlist.backend.dto.request.admin.RewardTierRequest;
import com.restaurant.waitlist.backend.dto.request.admin.WayToEarnRequest;
import com.restaurant.waitlist.backend.dto.response.admin.RewardTierResponse;
import com.restaurant.waitlist.backend.entity.RewardSettings;
import com.restaurant.waitlist.backend.entity.RewardTier;
import com.restaurant.waitlist.backend.entity.WayToEarn;
import com.restaurant.waitlist.backend.repository.AuditLogRepository;
import com.restaurant.waitlist.backend.repository.RewardSettingsRepository;
import com.restaurant.waitlist.backend.repository.RewardTierRepository;
import com.restaurant.waitlist.backend.repository.WayToEarnRepository;
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
    private final WayToEarnRepository wayToEarnRepository;
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
    @Transactional
    public WayToEarnRequest createWayToEarn(WayToEarnRequest request) {
        WayToEarn w = WayToEarn.builder().action(request.getAction()).points(request.getPoints()).build();
        wayToEarnRepository.save(w);
        auditLogRepository.save(com.restaurant.waitlist.backend.entity.AuditLog.builder()
                .restaurantId(0L)
                .action("CREATE_WAY_TO_EARN")
                .details("Action: " + request.getAction())
                .build());
        return request;
    }

    @Override
    public List<WayToEarnRequest> listWaysToEarn() {
        return wayToEarnRepository.findAll().stream().map(w -> {
            WayToEarnRequest r = new WayToEarnRequest();
            r.setAction(w.getAction());
            r.setPoints(w.getPoints());
            return r;
        }).collect(Collectors.toList());
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
}
