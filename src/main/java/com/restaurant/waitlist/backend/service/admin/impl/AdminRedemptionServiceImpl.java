package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.response.admin.RedemptionResponse;
import com.restaurant.waitlist.backend.entity.Campaign;
import com.restaurant.waitlist.backend.entity.Redemption;
import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.repository.CampaignRepository;
import com.restaurant.waitlist.backend.repository.RedemptionRepository;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.service.AdminLocationAccessService;
import com.restaurant.waitlist.backend.service.admin.AdminRedemptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminRedemptionServiceImpl implements AdminRedemptionService {

    private final RedemptionRepository redemptionRepository;
    private final CampaignRepository campaignRepository;
    private final RestaurantRepository restaurantRepository;
    private final AdminLocationAccessService adminLocationAccessService;

    @Override
    public Page<RedemptionResponse> listRedemptions(Long locationId, String status, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        List<Long> restaurantIds = adminLocationAccessService.resolveRestaurantIds(locationId);
        Page<Redemption> page = redemptionRepository.findFiltered(restaurantIds, from, to, null, null, pageable);
        Map<Long, String> restaurantNames = restaurantNamesFor(page.getContent());
        return page.map(r -> map(r, restaurantNames));
    }

    @Override
    public void exportRedemptionsCsv(Long locationId, String status, LocalDateTime from, LocalDateTime to, OutputStream out) throws java.io.IOException {
        List<Long> restaurantIds = adminLocationAccessService.resolveRestaurantIds(locationId);
        // stream using pagination to avoid loading everything into memory
        int page = 0;
        int size = 500;
        try (PrintWriter writer = new PrintWriter(out)) {
            writer.println("id,type,itemRedeemed,location,guest,mobileNumber,redeemedAt,value");
            org.springframework.data.domain.Page<Redemption> p;
            do {
                p = redemptionRepository.findFiltered(restaurantIds, from, to, null, null, org.springframework.data.domain.PageRequest.of(page, size));
                Map<Long, String> restaurantNames = restaurantNamesFor(p.getContent());
                for (Redemption r : p.getContent()) {
                    String item = resolveItemRedeemed(r);
                    String location = r.getRestaurantId() != null ? restaurantNames.get(r.getRestaurantId()) : null;
                    String line = String.format("%d,%s,%s,%s,%s,%s,%s,%s",
                            r.getId(),
                            resolveType(r),
                            item != null ? item.replaceAll(",", " ") : "",
                            location != null ? location.replaceAll(",", " ") : "",
                            r.getGuestName() != null ? r.getGuestName().replaceAll(",", " ") : "",
                            r.getGuestPhone() != null ? r.getGuestPhone() : "",
                            r.getRedeemedAt() != null ? r.getRedeemedAt().toString() : "",
                            r.getValue() != null ? r.getValue().toPlainString() : "0"
                    );
                    writer.println(line);
                }
                writer.flush();
                page++;
            } while (!p.isLast());
        }
    }

    private Map<Long, String> restaurantNamesFor(List<Redemption> redemptions) {
        Set<Long> ids = redemptions.stream()
                .map(Redemption::getRestaurantId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return restaurantRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Restaurant::getId, Restaurant::getName));
    }

    private String resolveType(Redemption r) {
        if (r.getCampaign() != null) return "CAMPAIGN";
        if (r.getOffer() != null) return "OFFER";
        if (r.getRewardItemId() != null) return "REWARD";
        return "UNKNOWN";
    }

    private String resolveItemRedeemed(Redemption r) {
        if (r.getOffer() != null) return r.getOffer().getName();
        if (r.getCampaign() != null) return r.getCampaign().getName();
        return null;
    }

    private RedemptionResponse map(Redemption r, Map<Long, String> restaurantNames) {
        RedemptionResponse resp = new RedemptionResponse();
        resp.setId(r.getId());
        resp.setType(resolveType(r));
        resp.setItemRedeemed(resolveItemRedeemed(r));
        resp.setLocation(r.getRestaurantId() != null ? restaurantNames.get(r.getRestaurantId()) : null);
        resp.setGuest(r.getGuestName());
        resp.setMobileNumber(r.getGuestPhone());
        resp.setRedeemedAt(r.getRedeemedAt());
        resp.setValue(r.getValue());
        resp.setPointsRedeemed(null);
        return resp;
    }

    private RedemptionResponse map(Redemption r) {
        Map<Long, String> restaurantNames = r.getRestaurantId() != null
                ? restaurantRepository.findById(r.getRestaurantId())
                        .map(rest -> Map.of(r.getRestaurantId(), rest.getName()))
                        .orElse(Map.of())
                : Map.of();
        return map(r, restaurantNames);
    }

    @Override
    public RedemptionResponse getRedemptionById(Long redemptionId) {
        Redemption r = redemptionRepository.findById(redemptionId)
            .orElseThrow(() -> new IllegalArgumentException("Redemption not found"));
        adminLocationAccessService.assertAccess(r.getRestaurantId());
        return map(r);
    }

    @Override
    public RedemptionResponse cancelRedemption(Long redemptionId, String reason) {
        Redemption r = redemptionRepository.findById(redemptionId)
            .orElseThrow(() -> new IllegalArgumentException("Redemption not found"));
        adminLocationAccessService.assertAccess(r.getRestaurantId());
        r.setStatus(Redemption.RedemptionStatus.CANCELLED);
        redemptionRepository.save(r);
        return map(r);
    }

    @Override
    public java.util.Map<String, Object> expireBulkRedemptions(java.util.List<Long> redemptionIds, String reason) {
        return java.util.Map.of("expired", redemptionIds.size());
    }

    @Override
    public java.util.Map<String, Object> getStatistics(Long locationId, LocalDateTime from, LocalDateTime to) {
        adminLocationAccessService.assertAccess(locationId);
        return java.util.Map.of(
            "totalRedemptions", 0,
            "totalValue", 0
        );
    }

    @Override
    public Page<RedemptionResponse> getByOffer(Long offerId, Pageable pageable) {
        List<Long> restaurantIds = adminLocationAccessService.getAccessibleRestaurantIds();
        Page<Redemption> page = redemptionRepository.findFiltered(restaurantIds, null, null, offerId, null, pageable);
        Map<Long, String> restaurantNames = restaurantNamesFor(page.getContent());
        return page.map(r -> map(r, restaurantNames));
    }

    @Override
    public Page<RedemptionResponse> getByUser(Long userId, Pageable pageable) {
        List<Long> restaurantIds = adminLocationAccessService.getAccessibleRestaurantIds();
        Page<Redemption> page = redemptionRepository.findFiltered(restaurantIds, null, null, null, userId, pageable);
        Map<Long, String> restaurantNames = restaurantNamesFor(page.getContent());
        return page.map(r -> map(r, restaurantNames));
    }

    @Override
    @Transactional
    public java.util.Map<String, Object> validateAndCompleteCampaignCodeByCode(String code, Long restaurantId, String guestPhone) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();

        try {
            if (guestPhone == null || guestPhone.isBlank()) {
                throw new IllegalArgumentException("Guest phone is required");
            }

            // The shared coupon code lives on the campaign itself, not on any one redemption row.
            Campaign campaign = campaignRepository.findByRedemptionCode(code)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid or expired campaign code"));

            if (!"ACTIVE".equalsIgnoreCase(campaign.getStatus())
                    || (campaign.getEndDate() != null && campaign.getEndDate().isBefore(LocalDateTime.now()))) {
                throw new IllegalArgumentException("Invalid or expired campaign code");
            }

            // A campaign scoped to one restaurant can only be redeemed there; a
            // franchise-wide campaign (restaurantId == null) can be redeemed anywhere.
            if (campaign.getRestaurantId() != null && !campaign.getRestaurantId().equals(restaurantId)) {
                throw new IllegalArgumentException("Invalid or expired campaign code");
            }

            boolean alreadyRedeemed = redemptionRepository.existsByCampaignIdAndGuestPhoneAndStatus(
                    campaign.getId(), guestPhone, Redemption.RedemptionStatus.COMPLETED);
            if (alreadyRedeemed) {
                throw new IllegalArgumentException("This code has already been redeemed by this guest");
            }

            Redemption redemption = Redemption.builder()
                    .campaign(campaign)
                    .redemptionCode(code)
                    .guestPhone(guestPhone)
                    .status(Redemption.RedemptionStatus.COMPLETED)
                    .restaurantId(restaurantId)
                    .redeemedAt(LocalDateTime.now())
                    .build();
            redemptionRepository.save(redemption);

            response.put("success", true);
            response.put("message", "Campaign code validated and completed");
            response.put("code", code);
            response.put("campaignId", campaign.getId());
            response.put("redemptionId", redemption.getId());
            response.put("guestPhone", guestPhone);
            response.put("offer", campaign.getName());
            response.put("offerMessage", campaign.getMessage());
            response.put("status", "COMPLETED");

        } catch (IllegalArgumentException ex) {
            response.put("success", false);
            response.put("message", ex.getMessage());
            response.put("code", code);
            response.put("error", ex.getMessage());
        }

        return response;
    }
}

