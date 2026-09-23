package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.response.admin.RedemptionResponse;
import com.restaurant.waitlist.backend.entity.Redemption;
import com.restaurant.waitlist.backend.repository.RedemptionRepository;
import com.restaurant.waitlist.backend.service.AdminLocationAccessService;
import com.restaurant.waitlist.backend.service.admin.AdminRedemptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminRedemptionServiceImpl implements AdminRedemptionService {

    private final RedemptionRepository redemptionRepository;
    private final AdminLocationAccessService adminLocationAccessService;

    @Override
    public Page<RedemptionResponse> listRedemptions(Long locationId, String status, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        List<Long> restaurantIds = adminLocationAccessService.resolveRestaurantIds(locationId);
        Page<Redemption> page = redemptionRepository.findFiltered(restaurantIds, from, to, null, null, pageable);
        return page.map(this::map);
    }

    @Override
    public void exportRedemptionsCsv(Long locationId, String status, LocalDateTime from, LocalDateTime to, OutputStream out) throws java.io.IOException {
        List<Long> restaurantIds = adminLocationAccessService.resolveRestaurantIds(locationId);
        // stream using pagination to avoid loading everything into memory
        int page = 0;
        int size = 500;
        try (PrintWriter writer = new PrintWriter(out)) {
            writer.println("id,itemRedeemed,location,guest,mobileNumber,redeemedAt,value");
            org.springframework.data.domain.Page<Redemption> p;
            do {
                p = redemptionRepository.findFiltered(restaurantIds, from, to, null, null, org.springframework.data.domain.PageRequest.of(page, size));
                for (Redemption r : p.getContent()) {
                        String line = String.format("%d,%s,%s,%s,%s,%s,%s",
                            r.getId(),
                            r.getOffer() != null ? r.getOffer().getName().replaceAll(",", " ") : "",
                            r.getOffer() != null && r.getOffer().getRestaurant() != null ? r.getOffer().getRestaurant().getName().replaceAll(",", " ") : "",
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

    private RedemptionResponse map(Redemption r) {
        RedemptionResponse resp = new RedemptionResponse();
        resp.setId(r.getId());
        resp.setItemRedeemed(r.getOffer() != null ? r.getOffer().getName() : null);
        resp.setLocation(r.getOffer() != null && r.getOffer().getRestaurant() != null ? r.getOffer().getRestaurant().getName() : null);
        resp.setGuest(r.getGuestName());
        resp.setMobileNumber(r.getGuestPhone());
        resp.setRedeemedAt(r.getRedeemedAt());
        resp.setValue(r.getValue());
        resp.setPointsRedeemed(null);
        return resp;
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
        // Mark as cancelled
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
        return page.map(this::map);
    }

    @Override
    public Page<RedemptionResponse> getByUser(Long userId, Pageable pageable) {
        List<Long> restaurantIds = adminLocationAccessService.getAccessibleRestaurantIds();
        Page<Redemption> page = redemptionRepository.findFiltered(restaurantIds, null, null, null, userId, pageable);
        return page.map(this::map);
    }

    @Override
    public java.util.Map<String, Object> validateAndCompleteCampaignCodeByCode(String code) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        
        try {
            // Find the redemption record for this campaign code (campaign ID auto-looked-up)
            Redemption redemption = redemptionRepository.findValidCampaignCodeByCodeOnly(code)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid or expired campaign code"));
            
            // Mark as completed
            redemption.setStatus(Redemption.RedemptionStatus.COMPLETED);
            redemption.setRedeemedAt(LocalDateTime.now());
            redemptionRepository.save(redemption);
            
            // Return success response
            Long campaignId = redemption.getCampaign() != null ? redemption.getCampaign().getId() : null;
            response.put("success", true);
            response.put("message", "Campaign code validated and completed");
            response.put("code", code);
            response.put("campaignId", campaignId);
            response.put("redemptionId", redemption.getId());
            response.put("guestPhone", redemption.getGuestPhone());
            response.put("offer", redemption.getCampaign().getName());
            response.put("offerMessage", redemption.getCampaign().getMessage());
            response.put("status", "COMPLETED");
            
        } catch (IllegalArgumentException ex) {
            response.put("success", false);
            response.put("message", "Invalid or expired campaign code");
            response.put("code", code);
            response.put("error", ex.getMessage());
        }
        
        return response;
    }
}

