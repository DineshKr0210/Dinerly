package com.restaurant.waitlist.backend.controller.admin;

import com.restaurant.waitlist.backend.dto.request.admin.ApproveReceiptRequest;
import com.restaurant.waitlist.backend.dto.request.admin.RejectReceiptRequest;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.admin.ReceiptClaimResponse;
import com.restaurant.waitlist.backend.service.admin.AdminReceiptClaimService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/admin/receipt-claims")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReceiptClaimController {

    private final AdminReceiptClaimService adminReceiptClaimService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReceiptClaimResponse>>> list(
            @RequestParam(required = false) Long restaurantId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReceiptClaimResponse> resp = adminReceiptClaimService.listReceiptClaims(restaurantId, status, from, to, pageable);
        return ResponseEntity.ok(ApiResponse.success("Receipt claims retrieved successfully", resp));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<Page<ReceiptClaimResponse>>> listPending(
            @RequestParam(required = false) Long restaurantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReceiptClaimResponse> resp = adminReceiptClaimService.listPendingClaims(restaurantId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Pending receipt claims retrieved successfully", resp));
    }

    @GetMapping("/{claimId}")
    public ResponseEntity<ApiResponse<ReceiptClaimResponse>> getById(@PathVariable Long claimId) {
        ReceiptClaimResponse resp = adminReceiptClaimService.getReceiptClaimById(claimId);
        return ResponseEntity.ok(ApiResponse.success("Receipt claim retrieved successfully", resp));
    }

    @GetMapping("/{claimId}/details")
    public ResponseEntity<ApiResponse<ReceiptClaimResponse>> getDetails(@PathVariable Long claimId) {
        ReceiptClaimResponse resp = adminReceiptClaimService.getReceiptClaimDetails(claimId);
        return ResponseEntity.ok(ApiResponse.success("Receipt claim details retrieved successfully", resp));
    }

    @PutMapping("/{claimId}/approve")
    public ResponseEntity<ApiResponse<ReceiptClaimResponse>> approve(
            @PathVariable Long claimId,
            @Valid @RequestBody ApproveReceiptRequest request) {
        ReceiptClaimResponse resp = adminReceiptClaimService.approveReceiptClaim(claimId, request);
        return ResponseEntity.ok(ApiResponse.success("Receipt claim approved successfully", resp));
    }

    @PutMapping("/{claimId}/reject")
    public ResponseEntity<ApiResponse<ReceiptClaimResponse>> reject(
            @PathVariable Long claimId,
            @Valid @RequestBody RejectReceiptRequest request) {
        ReceiptClaimResponse resp = adminReceiptClaimService.rejectReceiptClaim(claimId, request);
        return ResponseEntity.ok(ApiResponse.success("Receipt claim rejected successfully", resp));
    }

    @PostMapping("/{claimId}/approve-bulk")
    public ResponseEntity<ApiResponse<Map<String, Object>>> approveBulk(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Long> claimIds = (List<Long>) request.get("claimIds");
        Long pointsOverride = request.get("pointsOverride") != null ? 
            Long.valueOf(request.get("pointsOverride").toString()) : null;
        Map<String, Object> resp = adminReceiptClaimService.approveBulkClaims(claimIds, pointsOverride);
        return ResponseEntity.ok(ApiResponse.success("Bulk approval completed successfully", resp));
    }

    @PostMapping("/{claimId}/reject-bulk")
    public ResponseEntity<ApiResponse<Map<String, Object>>> rejectBulk(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Long> claimIds = (List<Long>) request.get("claimIds");
        String reason = (String) request.get("reason");
        Map<String, Object> resp = adminReceiptClaimService.rejectBulkClaims(claimIds, reason);
        return ResponseEntity.ok(ApiResponse.success("Bulk rejection completed successfully", resp));
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<ApiResponse<Page<ReceiptClaimResponse>>> getByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReceiptClaimResponse> resp = adminReceiptClaimService.getByUser(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("User receipts retrieved successfully", resp));
    }

    @GetMapping("/by-restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse<Page<ReceiptClaimResponse>>> getByRestaurant(
            @PathVariable Long restaurantId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReceiptClaimResponse> resp = adminReceiptClaimService.getByRestaurant(restaurantId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Restaurant receipts retrieved successfully", resp));
    }

    @GetMapping("/duplicates")
    public ResponseEntity<ApiResponse<Page<ReceiptClaimResponse>>> getDuplicates(
            @RequestParam(required = false) Long restaurantId,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReceiptClaimResponse> resp = adminReceiptClaimService.getDuplicateClaims(restaurantId, userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Duplicate claims retrieved successfully", resp));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) Long restaurantId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        adminReceiptClaimService.exportReceiptClaimsCsv(restaurantId, status, from, to, out);
        byte[] csv = out.toByteArray();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=receipt-claims.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics(
            @RequestParam(required = false) Long restaurantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        Map<String, Object> resp = adminReceiptClaimService.getStatistics(restaurantId, from, to);
        return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", resp));
    }

    @PostMapping("/{claimId}/mark-duplicate")
    public ResponseEntity<ApiResponse<ReceiptClaimResponse>> markDuplicate(
            @PathVariable Long claimId,
            @RequestParam(required = false) Long duplicateOfId) {
        ReceiptClaimResponse resp = adminReceiptClaimService.markAsDuplicate(claimId, duplicateOfId);
        return ResponseEntity.ok(ApiResponse.success("Claim marked as duplicate successfully", resp));
    }

    @PostMapping("/{claimId}/revert")
    public ResponseEntity<ApiResponse<ReceiptClaimResponse>> revert(@PathVariable Long claimId) {
        ReceiptClaimResponse resp = adminReceiptClaimService.revertClaim(claimId);
        return ResponseEntity.ok(ApiResponse.success("Claim reverted to pending successfully", resp));
    }
}
