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
    RedemptionResponse getRedemptionByCode(String code);
    Page<RedemptionResponse> getByStatus(String status, Long locationId, Pageable pageable);
    Page<RedemptionResponse> getExpiredCodes(Long locationId, Pageable pageable);
    RedemptionResponse cancelRedemption(Long redemptionId, String reason);
    RedemptionResponse expireRedemption(Long redemptionId);
    Map<String, Object> expireBulkRedemptions(List<Long> redemptionIds, String reason);
    void exportRedemptionsCsv(Long locationId, String status, LocalDateTime from, LocalDateTime to, java.io.OutputStream out) throws java.io.IOException;
    Map<String, Object> getStatistics(Long locationId, LocalDateTime from, LocalDateTime to);
    Page<RedemptionResponse> getByOffer(Long offerId, Pageable pageable);
    Page<RedemptionResponse> getByUser(Long userId, Pageable pageable);
}
