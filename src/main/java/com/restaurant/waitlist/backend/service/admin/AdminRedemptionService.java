package com.restaurant.waitlist.backend.service.admin;

import com.restaurant.waitlist.backend.dto.response.admin.RedemptionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AdminRedemptionService {
    Page<RedemptionResponse> listRedemptions(Long locationId, String status, LocalDateTime from, LocalDateTime to, Pageable pageable);
    RedemptionResponse getRedemptionById(Long redemptionId);
    RedemptionResponse cancelRedemption(Long redemptionId, String reason);
    Map<String, Object> expireBulkRedemptions(List<Long> redemptionIds, String reason);
    void exportRedemptionsCsv(Long locationId, String status, LocalDateTime from, LocalDateTime to, java.io.OutputStream out) throws java.io.IOException;
    Map<String, Object> getStatistics(Long locationId, LocalDateTime from, LocalDateTime to);
    Page<RedemptionResponse> getByOffer(Long offerId, Pageable pageable);
    Page<RedemptionResponse> getByUser(Long userId, Pageable pageable);
    /**
     * A campaign scoped to a single restaurant can only be redeemed at that
     * restaurant; a franchise-wide campaign can be redeemed at any of the
     * admin's locations. The guest phone is required so the same shared code
     * can be tracked per-guest and can't be redeemed twice by the same guest.
     */
    Map<String, Object> validateAndCompleteCampaignCodeByCode(String code, Long restaurantId, String guestPhone);
}
