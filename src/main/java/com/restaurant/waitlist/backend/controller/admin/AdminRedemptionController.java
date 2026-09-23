package com.restaurant.waitlist.backend.controller.admin;

import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.admin.RedemptionResponse;
import com.restaurant.waitlist.backend.service.admin.AdminRedemptionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/redemptions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Redemptions", description = "View, cancel, and export offer/campaign/reward redemptions")
public class AdminRedemptionController {

    private final AdminRedemptionService adminRedemptionService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<RedemptionResponse>>> list(
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RedemptionResponse> resp = adminRedemptionService.listRedemptions(locationId, status, from, to, pageable);
        return ResponseEntity.ok(ApiResponse.success("Data retrieved successfully", resp));
    }

    @GetMapping("/{redemptionId}")
    public ResponseEntity<ApiResponse<RedemptionResponse>> getById(@PathVariable Long redemptionId) {
        RedemptionResponse resp = adminRedemptionService.getRedemptionById(redemptionId);
        return ResponseEntity.ok(ApiResponse.success("Redemption retrieved successfully", resp));
    }



    @PutMapping("/{redemptionId}/cancel")
    public ResponseEntity<ApiResponse<RedemptionResponse>> cancelRedemption(
            @PathVariable Long redemptionId,
            @RequestParam(required = false) String reason) {
        RedemptionResponse resp = adminRedemptionService.cancelRedemption(redemptionId, reason);
        return ResponseEntity.ok(ApiResponse.success("Redemption cancelled successfully", resp));
    }


    @PostMapping("/expire-bulk")
    public ResponseEntity<ApiResponse<Map<String, Object>>> expireBulk(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Long> redemptionIds = (List<Long>) request.get("redemptionIds");
        String reason = (String) request.get("reason");
        Map<String, Object> resp = adminRedemptionService.expireBulkRedemptions(redemptionIds, reason);
        return ResponseEntity.ok(ApiResponse.success("Bulk expiry completed successfully", resp));
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        adminRedemptionService.exportRedemptionsCsv(locationId, status, from, to, out);
        byte[] csv = out.toByteArray();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=redemptions.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics(
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        Map<String, Object> resp = adminRedemptionService.getStatistics(locationId, from, to);
        return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", resp));
    }

    @GetMapping("/by-offer/{offerId}")
    public ResponseEntity<ApiResponse<Page<RedemptionResponse>>> getByOffer(
            @PathVariable Long offerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RedemptionResponse> resp = adminRedemptionService.getByOffer(offerId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Redemptions retrieved by offer successfully", resp));
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<ApiResponse<Page<RedemptionResponse>>> getByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RedemptionResponse> resp = adminRedemptionService.getByUser(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Redemptions retrieved by user successfully", resp));
    }
}

