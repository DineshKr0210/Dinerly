package com.restaurant.waitlist.backend.controller;

import com.restaurant.waitlist.backend.entity.Offer;
import com.restaurant.waitlist.backend.entity.Redemption;
import com.restaurant.waitlist.backend.entity.User;
import com.restaurant.waitlist.backend.repository.OfferRepository;
import com.restaurant.waitlist.backend.repository.RedemptionRepository;
import com.restaurant.waitlist.backend.repository.UserRepository;
import com.restaurant.waitlist.backend.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GuestPointsController {
    private static final Logger log = LoggerFactory.getLogger(GuestPointsController.class);

    private final PointsService pointsService;
    private final UserRepository userRepository;
    private final OfferRepository offerRepository;
    private final RedemptionRepository redemptionRepository;

    @GetMapping("/me/points")
    public ResponseEntity<?> myPoints() {
        String principal = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(principal).orElseThrow(() -> new RuntimeException("User not found"));
        long balance = pointsService.getBalance(user.getId());
        return ResponseEntity.ok().body(balance);
    }

    @PostMapping("/offers/{offerId}/redeem")
    public ResponseEntity<?> redeemOffer(@PathVariable Long offerId) {
        String principal = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(principal).orElseThrow(() -> new RuntimeException("User not found"));
        Offer offer = offerRepository.findById(offerId).orElseThrow(() -> new RuntimeException("Offer not found"));
        if (offer.getPointsCost() == null) return ResponseEntity.badRequest().body("Offer not configured for points redemption");
        long cost = offer.getPointsCost();
        try {
            pointsService.debit(user.getId(), cost, "Redeem offer " + offer.getId(), "OFFER", offer.getId());
            Redemption r = Redemption.builder()
                    .offer(offer)
                    .restaurantId(offer.getRestaurant().getId())
                    .guestName(user.getName())
                    .guestPhone(user.getPhone())
                    .value(offer.getValue())
                    .build();
            redemptionRepository.save(r);
            return ResponseEntity.ok().body("Redeemed successfully");
        } catch (PointsService.InsufficientPointsException e) {
            return ResponseEntity.status(402).body("Insufficient points");
        }
    }
}
