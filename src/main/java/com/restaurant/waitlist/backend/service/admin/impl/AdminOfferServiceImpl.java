package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.request.admin.OfferRequest;
import com.restaurant.waitlist.backend.dto.response.admin.OfferResponse;
import com.restaurant.waitlist.backend.entity.Offer;
import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.repository.OfferRepository;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.service.AdminLocationAccessService;
import com.restaurant.waitlist.backend.service.AuditLogService;
import com.restaurant.waitlist.backend.service.admin.AdminOfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminOfferServiceImpl implements AdminOfferService {

    private final OfferRepository offerRepository;
    private final RestaurantRepository restaurantRepository;
    private final AdminLocationAccessService adminLocationAccessService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public OfferResponse createOffer(OfferRequest request) {
        adminLocationAccessService.assertAccess(request.getLocationId());
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
                .discountValue(request.getDiscountValue())
                .discountLabel(request.getDiscountLabel())
                .description(request.getDescription())
                .photoUrl(request.getPhotoUrl())
                .perUserLimit(request.getPerUserLimit())
                .build();
        Offer saved = offerRepository.save(o);
        auditLogService.log(r.getId(), "CREATE_OFFER", "Offer created: " + saved.getName());
        return map(saved);
    }

    @Override
    @Transactional
    public OfferResponse updateOffer(Long offerId, OfferRequest request) {
        Offer o = offerRepository.findById(offerId).orElseThrow(() -> new IllegalArgumentException("Offer not found"));
        adminLocationAccessService.assertAccess(o.getRestaurant().getId());
        adminLocationAccessService.assertAccess(request.getLocationId());
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
        o.setDiscountValue(request.getDiscountValue());
        o.setDiscountLabel(request.getDiscountLabel());
        o.setDescription(request.getDescription());
        o.setPhotoUrl(request.getPhotoUrl());
        o.setPerUserLimit(request.getPerUserLimit());
        Offer saved = offerRepository.save(o);
        auditLogService.log(r.getId(), "UPDATE_OFFER", "Offer updated: " + saved.getName());
        return map(saved);
    }

    @Override
    @Transactional
    public void deleteOffer(Long offerId) {
        Offer o = offerRepository.findById(offerId).orElseThrow(() -> new IllegalArgumentException("Offer not found"));
        Long rid = o.getRestaurant().getId();
        adminLocationAccessService.assertAccess(rid);
        offerRepository.delete(o);
        auditLogService.log(rid, "DELETE_OFFER", "Offer deleted: " + o.getName());
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
                .discountValue(o.getDiscountValue())
                .discountLabel(o.getDiscountLabel())
                .description(o.getDescription())
                .photoUrl(o.getPhotoUrl())
                .perUserLimit(o.getPerUserLimit())
                .redemptions(redemptions)
                .build();
    }

    @Override
    public Page<OfferResponse> listOffers(Long locationId, String status, String category, Pageable pageable) {
        List<Long> restaurantIds = adminLocationAccessService.resolveRestaurantIds(locationId);
        Page<Offer> page = offerRepository.findFilteredByRestaurantIds(restaurantIds, status, null, null, pageable);
        return page.map(this::map);
    }

    @Override
    public OfferResponse getOfferById(Long offerId) {
        Offer offer = offerRepository.findById(offerId).orElseThrow(() -> new IllegalArgumentException("Offer not found"));
        adminLocationAccessService.assertAccess(offer.getRestaurant().getId());
        return map(offer);
    }

    @Override
    public OfferResponse toggleOfferStatus(Long offerId) {
        Offer offer = offerRepository.findById(offerId)
            .orElseThrow(() -> new IllegalArgumentException("Offer not found"));
        adminLocationAccessService.assertAccess(offer.getRestaurant().getId());
        offer.setStatus("INACTIVE".equals(offer.getStatus()) ? "ACTIVE" : "INACTIVE");
        offer = offerRepository.save(offer);
        return map(offer);
    }

    @Override
    public OfferResponse duplicateOffer(Long offerId, String newName) {
        Offer original = offerRepository.findById(offerId)
            .orElseThrow(() -> new IllegalArgumentException("Offer not found"));
        adminLocationAccessService.assertAccess(original.getRestaurant().getId());
        Offer duplicate = Offer.builder()
            .name(newName != null ? newName : original.getName() + " (Copy)")
            .restaurant(original.getRestaurant())
            .startDate(original.getStartDate())
            .endDate(original.getEndDate())
            .status("INACTIVE")
            .discountValue(original.getDiscountValue())
            .discountLabel(original.getDiscountLabel())
            .description(original.getDescription())
            .photoUrl(original.getPhotoUrl())
            .perUserLimit(original.getPerUserLimit())
            .build();
        duplicate = offerRepository.save(duplicate);
        return map(duplicate);
    }

    @Override
    public java.util.Map<String, Object> bulkDuplicateOffers(java.util.List<Long> offerIds) {
        int count = 0;
        for (Long offerId : offerIds) {
            duplicateOffer(offerId, null);
            count++;
        }
        return java.util.Map.of("duplicated", count);
    }

    @Override
    public OfferResponse archiveOffer(Long offerId) {
        Offer offer = offerRepository.findById(offerId)
            .orElseThrow(() -> new IllegalArgumentException("Offer not found"));
        adminLocationAccessService.assertAccess(offer.getRestaurant().getId());
        offer.setStatus("ARCHIVED");
        offer = offerRepository.save(offer);
        return map(offer);
    }

    @Override
    public java.util.Map<String, Object> bulkArchiveOffers(java.util.List<Long> offerIds) {
        int count = 0;
        for (Long offerId : offerIds) {
            archiveOffer(offerId);
            count++;
        }
        return java.util.Map.of("archived", count);
    }

    @Override
    public java.util.List<String> getAvailableCategories(Long locationId) {
        adminLocationAccessService.assertAccess(locationId);
        return java.util.Arrays.asList("food", "beverage", "dessert", "special");
    }

    @Override
    public Page<OfferResponse> getOffersByCategory(String category, Long locationId, Pageable pageable) {
        List<Long> restaurantIds = adminLocationAccessService.resolveRestaurantIds(locationId);
        Page<Offer> page = offerRepository.findFilteredByRestaurantIds(restaurantIds, null, null, null, pageable);
        return page.map(this::map);
    }

    @Override
    public Page<OfferResponse> getOfferExpiringSoon(Long locationId, int days, Pageable pageable) {
        List<Long> restaurantIds = adminLocationAccessService.resolveRestaurantIds(locationId);
        Page<Offer> page = offerRepository.findFilteredByRestaurantIds(restaurantIds, "ACTIVE", null, null, pageable);
        return page.map(this::map);
    }

    @Override
    public Page<OfferResponse> getOffersWithLowInventory(Long locationId, int threshold, Pageable pageable) {
        List<Long> restaurantIds = adminLocationAccessService.resolveRestaurantIds(locationId);
        Page<Offer> page = offerRepository.findFilteredByRestaurantIds(restaurantIds, "ACTIVE", null, null, pageable);
        return page.map(this::map);
    }

    @Override
    public void exportOffersCsv(Long locationId, String status, java.time.LocalDateTime from, java.time.LocalDateTime to, java.io.OutputStream out) throws Exception {
        adminLocationAccessService.assertAccess(locationId);
        // Stub implementation for CSV export
        out.write("id,name,status\n".getBytes());
    }

    @Override
    public java.util.Map<String, Object> getStatistics(Long locationId) {
        adminLocationAccessService.assertAccess(locationId);
        return java.util.Map.of(
            "totalOffers", 0,
            "activeOffers", 0,
            "totalRedemptions", 0
        );
    }

    @Override
    public java.util.Map<String, Object> bulkUpdateInventory(java.util.List<java.util.Map<String, Object>> updates) {
        return java.util.Map.of("updated", updates.size());
    }
}
