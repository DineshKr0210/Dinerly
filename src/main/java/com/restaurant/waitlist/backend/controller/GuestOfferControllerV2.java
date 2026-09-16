package com.restaurant.waitlist.backend.controller;

import com.restaurant.waitlist.backend.dto.request.RedeemOfferRequest;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.GuestOfferResponse;
import com.restaurant.waitlist.backend.dto.response.RedeemOfferResponse;
import com.restaurant.waitlist.backend.service.GuestOfferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
@Slf4j
public class GuestOfferController {

    private final GuestOfferService guestOfferService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<GuestOfferResponse>>> list(
        @RequestParam(required = false) Long locationId,
        @RequestParam(required = false) String category,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        try {
            Long userId = getCurrentUserId();
            Pageable pageable = PageRequest.of(page, size);

            Page<GuestOfferResponse> offers;
            if (category != null && !category.isEmpty()) {
                offers = guestOfferService.getOffersByLocationAndCategory(locationId, category, userId, pageable);
            } else {
                offers = guestOfferService.getOffersByLocation(locationId, userId, pageable);
            }

            return ResponseEntity.ok(ApiResponse.success(offers));
        } catch (Exception e) {
            log.error("Error fetching offers", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GuestOfferResponse>> get(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            GuestOfferResponse offer = guestOfferService.getOfferDetail(id, userId);
            return ResponseEntity.ok(ApiResponse.success(offer));
        } catch (Exception e) {
            log.error("Error fetching offer detail", e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/redeem")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<RedeemOfferResponse>> redeem(
        @PathVariable Long id,
        @RequestParam(required = false) Long restaurantId,
        @RequestBody(required = false) RedeemOfferRequest req
    ) {
        try {
            Long userId = getCurrentUserId();
            if (restaurantId == null && req != null) {
                restaurantId = req.getLocationId() != null ? req.getLocationId() : 1L; // Default to 1 if not provided
            }
            if (restaurantId == null) {
                restaurantId = 1L; // Default restaurant
            }

            RedeemOfferResponse response = guestOfferService.redeemOffer(id, userId, restaurantId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Error redeeming offer", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/redeem/{code}/confirm")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> confirmCode(@PathVariable String code) {
        try {
            guestOfferService.validateAndCompleteCode(code);
            return ResponseEntity.ok(ApiResponse.success("Code validated and redeemed"));
        } catch (Exception e) {
            log.error("Error validating code", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    private Long getCurrentUserId() {
        try {
            Object principal = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                // Extract user ID from principal or auth details
                // This assumes you have a way to map email/username to user ID
                return null; // Return null for anonymous users
            }
        } catch (Exception e) {
            log.debug("Error getting current user", e);
        }
        return null;
    }
}
