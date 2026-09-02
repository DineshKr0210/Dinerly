package com.restaurant.waitlist.backend.service.admin;

import com.restaurant.waitlist.backend.dto.response.admin.RedemptionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface AdminRedemptionService {
    Page<RedemptionResponse> listRedemptions(Long locationId, LocalDateTime from, LocalDateTime to, Pageable pageable);
    void exportRedemptionsCsv(Long locationId, LocalDateTime from, LocalDateTime to, java.io.OutputStream out) throws java.io.IOException;
}
