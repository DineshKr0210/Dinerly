package com.restaurant.waitlist.backend.controller;

import com.restaurant.waitlist.backend.dto.request.JoinWaitlistRequest;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.WaitlistResponse;
import com.restaurant.waitlist.backend.service.WaitlistService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<WaitlistResponse>> getWaitlistStatus(@RequestParam Long restaurantId, @RequestParam String phone) {
        try {
            WaitlistResponse response = waitlistService.getWaitlistStatus(restaurantId, phone);
            return ResponseEntity.ok(ApiResponse.success("Waitlist status", response));
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
}

