package com.restaurant.waitlist.backend.controller;

import com.restaurant.waitlist.backend.dto.request.JoinWaitlistRequest;
import com.restaurant.waitlist.backend.dto.request.WaitlistStatusRequest;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.DashboardStatsResponse;
import com.restaurant.waitlist.backend.dto.response.WaitlistDashboardStatsResponse;
import com.restaurant.waitlist.backend.dto.response.WaitlistResponse;
import com.restaurant.waitlist.backend.service.RestaurantService;
import com.restaurant.waitlist.backend.service.WaitlistService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/waitlist")
@CrossOrigin(origins = "*")
public class WaitlistController {

    @Autowired
    private WaitlistService waitlistService;

    @PostMapping
    public ResponseEntity<ApiResponse<WaitlistResponse>> joinWaitlist(@Valid @RequestBody JoinWaitlistRequest request) {
        try {
            WaitlistResponse response = waitlistService.joinWaitlist(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Successfully joined waitlist", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/status")
    public ResponseEntity<ApiResponse<WaitlistResponse>> getWaitlistStatus(@Valid @RequestBody WaitlistStatusRequest request) {
        try {
            // return the latest single waitlist entry for the given phone at the restaurant
            WaitlistResponse response = waitlistService.getWaitlistStatus(request.getRestaurantId(), request.getPhone());
            return ResponseEntity.ok(ApiResponse.success("Waitlist record found", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{restaurantId}/{id}")
    public ResponseEntity<ApiResponse<Void>> leaveWaitlist(@PathVariable Long restaurantId, @PathVariable Long id) {
        try {
            waitlistService.removeFromWaitlist(restaurantId, id);
            return ResponseEntity.ok(ApiResponse.success("Removed from waitlist"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    @GetMapping("/{restaurantId}/dashboard")
    public ResponseEntity<ApiResponse<WaitlistDashboardStatsResponse>> getDashboard(@PathVariable Long restaurantId) {
        try {
            WaitlistDashboardStatsResponse response = waitlistService.getDashboardStats(restaurantId);
            return ResponseEntity.ok(ApiResponse.success("Dashboard stats retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}

