package com.restaurant.waitlist.backend.controller.admin;

import com.restaurant.waitlist.backend.dto.request.admin.OfferRequest;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.admin.OfferResponse;
import com.restaurant.waitlist.backend.service.admin.AdminOfferService;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/admin/offers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Offers", description = "Create, manage, and analyze restaurant offers")
public class AdminOfferController {

    private final AdminOfferService adminOfferService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OfferResponse>>> list(@RequestParam(required = false) Long locationId,
                                                                 @RequestParam(required = false) String status,
                                                                 @RequestParam(required = false) String category,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OfferResponse> resp = adminOfferService.listOffers(locationId, status, category, pageable);
        return ResponseEntity.ok(ApiResponse.success("Data retrieved successfully", resp));
    }

    @GetMapping("/{offerId}")
    public ResponseEntity<ApiResponse<OfferResponse>> getById(@PathVariable Long offerId) {
        OfferResponse resp = adminOfferService.getOfferById(offerId);
        return ResponseEntity.ok(ApiResponse.success("Offer retrieved successfully", resp));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OfferResponse>> create(@Valid @RequestBody OfferRequest request) {
        OfferResponse resp = adminOfferService.createOffer(request);
        return ResponseEntity.ok(ApiResponse.success("Offer created successfully", resp));
    }

    @PutMapping("/{offerId}")
    public ResponseEntity<ApiResponse<OfferResponse>> update(@PathVariable Long offerId, @Valid @RequestBody OfferRequest request) {
        OfferResponse resp = adminOfferService.updateOffer(offerId, request);
        return ResponseEntity.ok(ApiResponse.success("Offer updated successfully", resp));
    }

    @DeleteMapping("/{offerId}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Long offerId) {
        adminOfferService.deleteOffer(offerId);
        return ResponseEntity.ok(ApiResponse.success("Offer deleted successfully"));
    }

    @PutMapping("/{offerId}/toggle-status")
    public ResponseEntity<ApiResponse<OfferResponse>> toggleStatus(@PathVariable Long offerId) {
        OfferResponse resp = adminOfferService.toggleOfferStatus(offerId);
        return ResponseEntity.ok(ApiResponse.success("Offer status toggled successfully", resp));
    }

    @PostMapping("/{offerId}/duplicate")
    public ResponseEntity<ApiResponse<OfferResponse>> duplicate(
            @PathVariable Long offerId,
            @RequestParam(required = false) String newName) {
        OfferResponse resp = adminOfferService.duplicateOffer(offerId, newName);
        return ResponseEntity.ok(ApiResponse.success("Offer duplicated successfully", resp));
    }

    @PostMapping("/bulk-duplicate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> bulkDuplicate(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Long> offerIds = (List<Long>) request.get("offerIds");
        Map<String, Object> resp = adminOfferService.bulkDuplicateOffers(offerIds);
        return ResponseEntity.ok(ApiResponse.success("Offers duplicated successfully", resp));
    }

    @PutMapping("/{offerId}/archive")
    public ResponseEntity<ApiResponse<OfferResponse>> archive(@PathVariable Long offerId) {
        OfferResponse resp = adminOfferService.archiveOffer(offerId);
        return ResponseEntity.ok(ApiResponse.success("Offer archived successfully", resp));
    }

    @PostMapping("/bulk-archive")
    public ResponseEntity<ApiResponse<Map<String, Object>>> bulkArchive(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Long> offerIds = (List<Long>) request.get("offerIds");
        Map<String, Object> resp = adminOfferService.bulkArchiveOffers(offerIds);
        return ResponseEntity.ok(ApiResponse.success("Offers archived successfully", resp));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories(
            @RequestParam(required = false) Long locationId) {
        List<String> resp = adminOfferService.getAvailableCategories(locationId);
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", resp));
    }

    @GetMapping("/by-category/{category}")
    public ResponseEntity<ApiResponse<Page<OfferResponse>>> getByCategory(
            @PathVariable String category,
            @RequestParam(required = false) Long locationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OfferResponse> resp = adminOfferService.getOffersByCategory(category, locationId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Offers retrieved by category successfully", resp));
    }

    @GetMapping("/expiring-soon")
    public ResponseEntity<ApiResponse<Page<OfferResponse>>> getExpiringSoon(
            @RequestParam(required = false) Long locationId,
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OfferResponse> resp = adminOfferService.getOfferExpiringSoon(locationId, days, pageable);
        return ResponseEntity.ok(ApiResponse.success("Expiring offers retrieved successfully", resp));
    }

    @GetMapping("/low-inventory")
    public ResponseEntity<ApiResponse<Page<OfferResponse>>> getLowInventory(
            @RequestParam(required = false) Long locationId,
            @RequestParam(defaultValue = "10") int threshold,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OfferResponse> resp = adminOfferService.getOffersWithLowInventory(locationId, threshold, pageable);
        return ResponseEntity.ok(ApiResponse.success("Low inventory offers retrieved successfully", resp));
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        adminOfferService.exportOffersCsv(locationId, status, from, to, out);
        byte[] csv = out.toByteArray();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=offers.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics(
            @RequestParam(required = false) Long locationId) {
        Map<String, Object> resp = adminOfferService.getStatistics(locationId);
        return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", resp));
    }

    @PostMapping("/bulk-update-inventory")
    public ResponseEntity<ApiResponse<Map<String, Object>>> bulkUpdateInventory(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> updates = (List<Map<String, Object>>) request.get("updates");
        Map<String, Object> resp = adminOfferService.bulkUpdateInventory(updates);
        return ResponseEntity.ok(ApiResponse.success("Inventory updated successfully", resp));
    }
}
