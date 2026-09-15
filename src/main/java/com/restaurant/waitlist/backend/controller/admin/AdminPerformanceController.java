package com.restaurant.waitlist.backend.controller.admin;

import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.admin.ReviewsPerformanceResponse;
import com.restaurant.waitlist.backend.dto.response.admin.RewardsOffersPerformanceResponse;
import com.restaurant.waitlist.backend.dto.response.admin.WaitlistPerformanceResponse;
import com.restaurant.waitlist.backend.service.admin.AdminPerformanceService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/performance")
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "bearerAuth")
public class AdminPerformanceController {

    @Autowired
    private AdminPerformanceService adminPerformanceService;

    @GetMapping("/waitlist")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WaitlistPerformanceResponse>> waitlistPerformance(
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false, defaultValue = "pastMonth") String period
    ) {
        WaitlistPerformanceResponse response = adminPerformanceService.getWaitlistPerformance(locationId, period);
        return ResponseEntity.ok(ApiResponse.success("Data retrieved successfully", response));
    }

    @GetMapping("/reviews")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReviewsPerformanceResponse>> reviewsPerformance(
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false, defaultValue = "pastMonth") String period
    ) {
        ReviewsPerformanceResponse response = adminPerformanceService.getReviewsPerformance(locationId, period);
        return ResponseEntity.ok(ApiResponse.success("Data retrieved successfully", response));
    }

    @GetMapping("/rewards-offers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RewardsOffersPerformanceResponse>> rewardsOffersPerformance(
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false, defaultValue = "pastMonth") String period,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        RewardsOffersPerformanceResponse response = adminPerformanceService.getRewardsOffersPerformance(locationId, period, page, size);
        return ResponseEntity.ok(ApiResponse.success("Data retrieved successfully", response));
    }
}
