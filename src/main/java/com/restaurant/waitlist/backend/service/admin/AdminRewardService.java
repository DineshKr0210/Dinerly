package com.restaurant.waitlist.backend.service.admin;

import com.restaurant.waitlist.backend.dto.request.admin.RewardSettingsRequest;
import com.restaurant.waitlist.backend.dto.request.admin.RewardTierRequest;
import com.restaurant.waitlist.backend.dto.request.admin.WayToEarnRequest;
import com.restaurant.waitlist.backend.dto.response.admin.RewardTierResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface AdminRewardService {
    Page<RewardTierResponse> listTiers(Long restaurantId, Pageable pageable);
    RewardTierResponse createTier(RewardTierRequest request);
    RewardTierResponse updateTier(Long tierId, RewardTierRequest request);
    void deleteTier(Long tierId);
    RewardTierResponse duplicateTier(Long tierId, String newName);

    RewardSettingsRequest getSettings();
    RewardSettingsRequest updateSettings(RewardSettingsRequest request);
    
    Map<String, Object> getStatistics(Long restaurantId);
    Map<String, Object> getUserTierDistribution(Long restaurantId);
}
