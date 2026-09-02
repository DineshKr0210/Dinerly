package com.restaurant.waitlist.backend.service.admin;

import com.restaurant.waitlist.backend.dto.request.admin.RewardSettingsRequest;
import com.restaurant.waitlist.backend.dto.request.admin.RewardTierRequest;
import com.restaurant.waitlist.backend.dto.request.admin.WayToEarnRequest;
import com.restaurant.waitlist.backend.dto.response.admin.RewardTierResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminRewardService {
    Page<RewardTierResponse> listTiers(Pageable pageable);
    RewardTierResponse createTier(RewardTierRequest request);
    RewardTierResponse updateTier(Long tierId, RewardTierRequest request);
    void deleteTier(Long tierId);

    List<RewardTierResponse> getAllTiers();

    WayToEarnRequest createWayToEarn(WayToEarnRequest request);
    List<WayToEarnRequest> listWaysToEarn();

    RewardSettingsRequest getSettings();
    RewardSettingsRequest updateSettings(RewardSettingsRequest request);
}
