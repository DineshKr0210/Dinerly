package com.restaurant.waitlist.backend.controller;

import com.restaurant.waitlist.backend.dto.response.admin.RewardTierResponse;
import com.restaurant.waitlist.backend.service.admin.AdminRewardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
public class GuestRewardController {

    private final AdminRewardService adminRewardService;

    @GetMapping("/tiers")
    public ResponseEntity<List<RewardTierResponse>> listTiers() {
        List<RewardTierResponse> resp = adminRewardService.getAllTiers();
        return ResponseEntity.ok(resp);
    }
}
