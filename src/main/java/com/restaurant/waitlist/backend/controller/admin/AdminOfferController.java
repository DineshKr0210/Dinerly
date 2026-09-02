package com.restaurant.waitlist.backend.controller.admin;

import com.restaurant.waitlist.backend.dto.request.admin.OfferRequest;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.admin.OfferResponse;
import com.restaurant.waitlist.backend.service.admin.AdminOfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/offers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOfferController {

    private final AdminOfferService adminOfferService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OfferResponse>>> list(@RequestParam(required = false) Long locationId,
                                                                 @RequestParam(required = false) String status,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OfferResponse> resp = adminOfferService.listOffers(locationId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Data retrieved successfully", resp));
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
}
