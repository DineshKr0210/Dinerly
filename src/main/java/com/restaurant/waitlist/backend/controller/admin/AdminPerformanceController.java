package com.restaurant.waitlist.backend.controller.admin;

import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.admin.WaitlistPerformanceSummary;
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
    public ResponseEntity<ApiResponse<WaitlistPerformanceSummary>> waitlistPerformance(
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String period
    ) {
        WaitlistPerformanceSummary summary = adminPerformanceService.getWaitlistPerformance(locationId, period);
        return ResponseEntity.ok(ApiResponse.success("Data retrieved successfully", summary));
    }

    @GetMapping("/reviews")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> reviewsPerformance(
            @RequestParam(required = false) String period
    ) {
        java.util.Map<String, Object> data = adminPerformanceService.getReviewsPerformance(period);
        return ResponseEntity.ok(ApiResponse.success("Data retrieved successfully", data));
    }
}
