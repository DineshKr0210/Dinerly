package com.restaurant.waitlist.backend.controller;

import com.restaurant.waitlist.backend.dto.response.GuestOfferResponse;
import com.restaurant.waitlist.backend.entity.Offer;
import com.restaurant.waitlist.backend.repository.OfferRepository;
import com.restaurant.waitlist.backend.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
public class GuestOfferController {

    private final OfferRepository offerRepository;
    private final PointsService pointsService;

    @GetMapping
    public ResponseEntity<Page<GuestOfferResponse>> list(@RequestParam(required = false) Long locationId,
                                                         @RequestParam(required = false) String status,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Offer> offers = offerRepository.findFiltered(locationId, status, null, null, pageable);

        // determine authenticated user's id if available
        Long userId = null;
        try {
            String principal = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal != null) {
                var u = userRepository.findByEmail(principal);
                if (u.isPresent()) userId = u.get().getId();
            }
        } catch (Exception ignored) {}

        final Long uid = userId;
        Page<GuestOfferResponse> resp = offers.map(o -> mapToDto(o, uid));
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GuestOfferResponse> get(@PathVariable Long id) {
        Optional<Offer> opt = offerRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Long userId = null;
        try {
            String principal = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal != null) {
                var u = userRepository.findByEmail(principal);
                if (u.isPresent()) userId = u.get().getId();
            }
        } catch (Exception ignored) {}

        return ResponseEntity.ok(mapToDto(opt.get(), userId));
    }

    private GuestOfferResponse mapToDto(Offer o, Long userId) {
        Long pointsCost = o.getPointsCost();
        Boolean redeemable = null;
        if (pointsCost != null) {
            if (userId == null) {
                redeemable = false;
            } else {
                long balance = pointsService.getBalance(userId);
                redeemable = balance >= pointsCost;
            }
        }
        return GuestOfferResponse.builder()
                .id(o.getId())
                .name(o.getName())
                .restaurantId(o.getRestaurant() != null ? o.getRestaurant().getId() : null)
                .restaurantName(o.getRestaurant() != null ? o.getRestaurant().getName() : null)
                .startDate(o.getStartDate())
                .endDate(o.getEndDate())
                .status(o.getStatus())
                .value(o.getValue())
                .pointsCost(pointsCost)
                .redeemable(redeemable)
                .build();
    }

    // need UserRepository to resolve principal -> userId
    private final com.restaurant.waitlist.backend.repository.UserRepository userRepository;
}
