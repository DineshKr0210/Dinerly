package com.restaurant.waitlist.backend.controller;

import com.restaurant.waitlist.backend.dto.request.RedeemOfferRequest;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.GuestOfferResponse;
import com.restaurant.waitlist.backend.dto.response.RedeemOfferResponse;
import com.restaurant.waitlist.backend.entity.User;
import com.restaurant.waitlist.backend.repository.UserRepository;
import com.restaurant.waitlist.backend.service.GuestOfferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
@Slf4j
public class GuestOfferController {

    private final GuestOfferService guestOfferService;
    private final UserRepository userRepository;

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

            return ResponseEntity.ok(ApiResponse.success("Offers retrieved successfully", offers));
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
            return ResponseEntity.ok(ApiResponse.success("Offer details retrieved successfully", offer));
        } catch (Exception e) {
            log.error("Error fetching offer detail", e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/redeem")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<RedeemOfferResponse>> redeem(
        @PathVariable Long id,
        @RequestParam(required = false) Long locationId
    ) {
        try {
            Long userId = getCurrentUserId();
            if (locationId == null) {
                locationId = 1L; // Default restaurant
            }

            RedeemOfferResponse response = guestOfferService.redeemOffer(id, userId, locationId);
            return ResponseEntity.ok(ApiResponse.success("Offer redeemed successfully", response));
        } catch (Exception e) {
            log.error("Error redeeming offer", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/redeem/{code}/confirm")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<com.restaurant.waitlist.backend.dto.response.ConfirmRedemptionResponse>> confirmCode(@PathVariable String code) {
        try {
            com.restaurant.waitlist.backend.dto.response.ConfirmRedemptionResponse response = guestOfferService.validateAndCompleteCode(code);
            return ResponseEntity.ok(ApiResponse.success("Code validated and redeemed", response));
        } catch (Exception e) {
            log.error("Error validating code", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }

            // Get the email from the principal (it's set by JwtFilter)
            String email = authentication.getPrincipal().toString();
            
            // Look up the user by email to get their ID
            User user = userRepository.findByEmail(email).orElse(null);
            
            if (user == null) {
                log.debug("User not found for email: {}", email);
                return null;
            }
            
            return user.getId();
        } catch (Exception e) {
            log.debug("Error getting current user", e);
        }
        return null;
    }
}
