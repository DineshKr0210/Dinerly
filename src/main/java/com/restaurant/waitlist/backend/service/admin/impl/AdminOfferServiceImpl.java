package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.request.admin.OfferRequest;
import com.restaurant.waitlist.backend.dto.response.admin.OfferResponse;
import com.restaurant.waitlist.backend.entity.Offer;
import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.repository.AuditLogRepository;
import com.restaurant.waitlist.backend.repository.OfferRepository;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.service.admin.AdminOfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminOfferServiceImpl implements AdminOfferService {

    private final OfferRepository offerRepository;
    private final RestaurantRepository restaurantRepository;
    private final AuditLogRepository auditLogRepository;

    @Override
    public Page<OfferResponse> listOffers(Long locationId, String status, Pageable pageable) {
        Page<Offer> page = offerRepository.findFiltered(locationId, status, null, null, pageable);
        return page.map(this::map);
    }

    @Override
    @Transactional
    public OfferResponse createOffer(OfferRequest request) {
        Restaurant r = restaurantRepository.findById(request.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("startDate must be <= endDate");
        }
        Offer o = Offer.builder()
                .name(request.getName())
                .restaurant(r)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus())
                .value(request.getValue())
                .build();
        Offer saved = offerRepository.save(o);
        auditLogRepository.save(com.restaurant.waitlist.backend.entity.AuditLog.builder()
                .restaurantId(r.getId())
                .action("CREATE_OFFER")
                .details("Offer created: " + saved.getName())
                .build());
        return map(saved);
    }

    @Override
    @Transactional
    public OfferResponse updateOffer(Long offerId, OfferRequest request) {
        Offer o = offerRepository.findById(offerId).orElseThrow(() -> new IllegalArgumentException("Offer not found"));
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("startDate must be <= endDate");
        }
        Restaurant r = restaurantRepository.findById(request.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));
        o.setName(request.getName());
        o.setRestaurant(r);
        o.setStartDate(request.getStartDate());
        o.setEndDate(request.getEndDate());
        o.setStatus(request.getStatus());
        o.setValue(request.getValue());
        Offer saved = offerRepository.save(o);
        auditLogRepository.save(com.restaurant.waitlist.backend.entity.AuditLog.builder()
                .restaurantId(r.getId())
                .action("UPDATE_OFFER")
                .details("Offer updated: " + saved.getName())
                .build());
        return map(saved);
    }

    @Override
    @Transactional
    public void deleteOffer(Long offerId) {
        Offer o = offerRepository.findById(offerId).orElseThrow(() -> new IllegalArgumentException("Offer not found"));
        Long rid = o.getRestaurant().getId();
        offerRepository.delete(o);
        auditLogRepository.save(com.restaurant.waitlist.backend.entity.AuditLog.builder()
                .restaurantId(rid)
                .action("DELETE_OFFER")
                .details("Offer deleted: " + o.getName())
                .build());
    }

    private OfferResponse map(Offer o) {
        long redemptions = offerRepository.countRedemptionsByOfferId(o.getId());
        return OfferResponse.builder()
                .id(o.getId())
                .name(o.getName())
                .locationId(o.getRestaurant().getId())
                .locationName(o.getRestaurant().getName())
                .startDate(o.getStartDate())
                .endDate(o.getEndDate())
                .status(o.getStatus())
                .value(o.getValue())
                .redemptions(redemptions)
                .build();
    }
}
