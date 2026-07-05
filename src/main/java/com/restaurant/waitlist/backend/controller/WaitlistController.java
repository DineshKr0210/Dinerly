package com.restaurant.waitlist.backend.controller;

import com.restaurant.waitlist.backend.dto.request.JoinWaitlistRequest;
import com.restaurant.waitlist.backend.dto.request.WaitlistStatusRequest;
import com.restaurant.waitlist.backend.dto.response.*;
import com.restaurant.waitlist.backend.service.RestaurantService;
import com.restaurant.waitlist.backend.service.WaitlistService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/waitlist")
@CrossOrigin(origins = "*")
public class WaitlistController {

    @Autowired
    private WaitlistService waitlistService;

    private static final Logger log = LoggerFactory.getLogger(WaitlistController.class);

    @PostMapping
    public ResponseEntity<ApiResponse<WaitlistResponse>> joinWaitlist(@Valid @RequestBody JoinWaitlistRequest request) {
        try {
            log.info("START: joinWaitlist | {}", request);
            WaitlistResponse response = waitlistService.joinWaitlist(request);
            log.info("END: joinWaitlist | success");
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Successfully joined waitlist", response));
        } catch (Exception e) {
            log.error("ERROR: joinWaitlist | {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/status")
    public ResponseEntity<ApiResponse<WaitlistResponse>> getWaitlistStatus(@Valid @RequestBody WaitlistStatusRequest request) {
        try {
            log.info("START: getWaitlistStatus | restaurantId={}, phone={}", request.getRestaurantId(), request.getPhone());
            WaitlistResponse response = waitlistService.getWaitlistStatus(request.getRestaurantId(), request.getPhone());
            log.info("END: getWaitlistStatus | success");
            return ResponseEntity.ok(ApiResponse.success("Waitlist record found", response));
        } catch (Exception e) {
            log.error("ERROR: getWaitlistStatus | {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{restaurantId}/{id}")
    public ResponseEntity<ApiResponse<Void>> leaveWaitlist(@PathVariable Long restaurantId, @PathVariable Long id) {
        try {
            log.info("START: leaveWaitlist | restaurantId={}, id={}", restaurantId, id);
            waitlistService.removeFromWaitlist(restaurantId, id);
            log.info("END: leaveWaitlist | success");
            return ResponseEntity.ok(ApiResponse.success("Removed from waitlist"));
        } catch (Exception e) {
            log.error("ERROR: leaveWaitlist | {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    @GetMapping("/{restaurantId}/dashboard")
    public ResponseEntity<ApiResponse<WaitlistDashboardStatsResponse>> getDashboard(@PathVariable Long restaurantId) {
        try {
            log.info("START: waitlist.getDashboard | restaurantId={}", restaurantId);
            WaitlistDashboardStatsResponse response = waitlistService.getDashboardStats(restaurantId);
            log.info("END: waitlist.getDashboard | success");
            return ResponseEntity.ok(ApiResponse.success("Dashboard stats retrieved", response));
        } catch (Exception e) {
            log.error("ERROR: waitlist.getDashboard | {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    @GetMapping("/restaurants")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getAllRestaurants() {
        try {
            log.info("START: getAllRestaurants | {}", "");
            List<RestaurantResponse> response = waitlistService.getAllRestaurants();
            log.info("END: getAllRestaurants | success");
            return ResponseEntity.ok(ApiResponse.success("Restaurants retrieved", response));
        } catch (Exception e) {
            log.error("ERROR: getAllRestaurants | {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}

