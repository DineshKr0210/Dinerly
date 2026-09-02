package com.restaurant.waitlist.backend.controller.admin;

import com.restaurant.waitlist.backend.dto.request.admin.RewardSettingsRequest;
import com.restaurant.waitlist.backend.dto.request.admin.RewardTierRequest;
import com.restaurant.waitlist.backend.dto.request.admin.WayToEarnRequest;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.admin.RewardTierResponse;
import com.restaurant.waitlist.backend.service.admin.AdminRewardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/rewards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRewardController {

    private final AdminRewardService adminRewardService;

    @GetMapping("/tiers")
    public ResponseEntity<ApiResponse<Page<RewardTierResponse>>> listTiers(@RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RewardTierResponse> resp = adminRewardService.listTiers(pageable);
        return ResponseEntity.ok(ApiResponse.success("Data retrieved successfully", resp));
    }

    @PostMapping("/tiers")
    public ResponseEntity<ApiResponse<RewardTierResponse>> createTier(@Valid @RequestBody RewardTierRequest request) {
        RewardTierResponse resp = adminRewardService.createTier(request);
        return ResponseEntity.ok(ApiResponse.success("Tier created successfully", resp));
    }

    @PutMapping("/tiers/{tierId}")
    public ResponseEntity<ApiResponse<RewardTierResponse>> updateTier(@PathVariable Long tierId, @Valid @RequestBody RewardTierRequest request) {
        RewardTierResponse resp = adminRewardService.updateTier(tierId, request);
        return ResponseEntity.ok(ApiResponse.success("Tier updated successfully", resp));
    }

    @DeleteMapping("/tiers/{tierId}")
    public ResponseEntity<ApiResponse<Object>> deleteTier(@PathVariable Long tierId) {
        adminRewardService.deleteTier(tierId);
        return ResponseEntity.ok(ApiResponse.success("Tier deleted successfully"));
    }

    @GetMapping("/ways-to-earn")
    public ResponseEntity<ApiResponse<List<WayToEarnRequest>>> waysToEarn() {
        List<WayToEarnRequest> resp = adminRewardService.listWaysToEarn();
        return ResponseEntity.ok(ApiResponse.success("Data retrieved successfully", resp));
    }

    @PostMapping("/ways-to-earn")
    public ResponseEntity<ApiResponse<WayToEarnRequest>> createWayToEarn(@Valid @RequestBody WayToEarnRequest request) {
        WayToEarnRequest resp = adminRewardService.createWayToEarn(request);
        return ResponseEntity.ok(ApiResponse.success("Way to earn created", resp));
    }

    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<RewardSettingsRequest>> getSettings() {
        RewardSettingsRequest resp = adminRewardService.getSettings();
        return ResponseEntity.ok(ApiResponse.success("Data retrieved successfully", resp));
    }

    @PutMapping("/settings")
    public ResponseEntity<ApiResponse<RewardSettingsRequest>> updateSettings(@Valid @RequestBody RewardSettingsRequest request) {
        RewardSettingsRequest resp = adminRewardService.updateSettings(request);
        return ResponseEntity.ok(ApiResponse.success("Settings updated successfully", resp));
    }
}
