package com.restaurant.waitlist.backend.controller;

import com.restaurant.waitlist.backend.dto.request.RedeemOfferRequest;
import com.restaurant.waitlist.backend.dto.request.ValidateOfferRequest;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.dto.response.GuestOfferResponse;
import com.restaurant.waitlist.backend.dto.response.RedeemOfferResponse;
import com.restaurant.waitlist.backend.service.CurrentUserResolver;
import com.restaurant.waitlist.backend.service.GuestOfferService;
import com.restaurant.waitlist.backend.util.QRCodeGenerator;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Guest Offers", description = "Browse restaurant offers and redeem them as a guest")
public class GuestOfferController {

    private final GuestOfferService guestOfferService;
    private final CurrentUserResolver currentUserResolver;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<GuestOfferResponse>>> list(
        @RequestParam(required = false) Long locationId,
        @RequestParam(required = false) String category,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        try {
            Long userId = currentUserResolver.getCurrentUserId();
            Pageable pageable = PageRequest.of(page, size);

            Page<GuestOfferResponse> offers;
            if (category != null && !category.isEmpty()) {
                offers = guestOfferService.getOffersByLocationAndCategory(locationId, category, userId, pageable);
            } else {
                offers = guestOfferService.getOffersByLocation(locationId, userId, pageable);
            }

            return ResponseEntity.ok(ApiResponse.success("Offers retrieved successfully", offers));
        } catch (Exception e) {
            log.error("Error fetching offers", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GuestOfferResponse>> get(@PathVariable Long id) {
        try {
            Long userId = currentUserResolver.getCurrentUserId();
            GuestOfferResponse offer = guestOfferService.getOfferDetail(id, userId);
            return ResponseEntity.ok(ApiResponse.success("Offer details retrieved successfully", offer));
        } catch (Exception e) {
            log.error("Error fetching offer detail", e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/redeem")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<RedeemOfferResponse>> redeem(
        @PathVariable Long id,
        @RequestParam(required = false) Long locationId
    ) {
        try {
            Long userId = currentUserResolver.getCurrentUserId();
            if (locationId == null) {
                locationId = 1L; // Default restaurant
            }

            RedeemOfferResponse response = guestOfferService.redeemOffer(id, userId, locationId);
            return ResponseEntity.ok(ApiResponse.success("Offer redeemed successfully", response));
        } catch (Exception e) {
            log.error("Error redeeming offer", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/redeem/{code}/confirm")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<com.restaurant.waitlist.backend.dto.response.ConfirmRedemptionResponse>> confirmCode(
            @PathVariable String code,
            @RequestParam Long restaurantId) {
        try {
            com.restaurant.waitlist.backend.dto.response.ConfirmRedemptionResponse response = guestOfferService.validateAndCompleteCode(code, restaurantId);
            return ResponseEntity.ok(ApiResponse.success("Code validated and redeemed", response));
        } catch (Exception e) {
            log.error("Error validating code", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Validate offer redemption using QR code scan OR manual code entry.
     * This endpoint supports:
     * 1. Manual code entry (redemptionCode field)
     * 2. QR code scanning (scannedQRText field - the decoded text from QR scanner)
     * 3. QR code data (qrCodeData field - base64 encoded QR image)
     *
     * POST /api/offers/validate
     * Body: {
     *   "redemptionCode": "123456",  // Manual entry
     *   OR
     *   "scannedQRText": "123456",   // From QR scanner app
     * }
     */
    @PostMapping("/validate")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<com.restaurant.waitlist.backend.dto.response.ConfirmRedemptionResponse>> validateOfferByCodeOrQR(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Contains either redemptionCode (manual) or scannedQRText (from QR scanner)")
            @RequestBody ValidateOfferRequest validateRequest,
            @RequestParam Long restaurantId) {
        try {
            log.info("Validating offer: manual code={}, QR scan={}", 
                validateRequest.getRedemptionCode() != null, 
                validateRequest.getScannedQRText() != null);
            
            com.restaurant.waitlist.backend.dto.response.ConfirmRedemptionResponse response = 
                ((GuestOfferService) guestOfferService).validateOfferByCodeOrQR(validateRequest, restaurantId);
            
            return ResponseEntity.ok(ApiResponse.success("Offer validated and redeemed successfully", response));
        } catch (Exception e) {
            log.error("Error validating offer by code or QR", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Scan and validate offer redemption using QR image from camera.
     * 
     * Upload a QR code image (PNG, JPG, etc.) taken from camera.
     * The server will decode the QR code and validate the offer.
     * 
     * POST /api/offers/scan-qr
     * Form: multipart/form-data
     * - qrImage: image file (PNG, JPG, GIF)
     * - restaurantId: the restaurant ID (query param)
     */
    @PostMapping("/scan-qr")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<com.restaurant.waitlist.backend.dto.response.ConfirmRedemptionResponse>> scanQRCodeAndValidate(
            @RequestParam("qrImage") MultipartFile qrImage,
            @RequestParam Long restaurantId) {
        try {
            log.info("Scanning QR code image: size={}bytes, contentType={}", 
                qrImage.getSize(), qrImage.getContentType());

            if (qrImage.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("QR image file is empty"));
            }

            // Decode QR code from image
            byte[] imageBytes = qrImage.getBytes();
            String decodedCode = QRCodeGenerator.decodeQRCode(imageBytes);

            log.info("QR code decoded: code={}", decodedCode.substring(0, 3) + "***");

            // Validate the decoded code
            ValidateOfferRequest validateRequest = ValidateOfferRequest.builder()
                    .scannedQRText(decodedCode)
                    .build();

            com.restaurant.waitlist.backend.dto.response.ConfirmRedemptionResponse response = 
                ((GuestOfferService) guestOfferService).validateOfferByCodeOrQR(validateRequest, restaurantId);

            return ResponseEntity.ok(ApiResponse.success("Offer validated successfully via QR scan", response));

        } catch (IOException e) {
            log.error("Error reading QR image file", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error reading image file: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error scanning QR code", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get QR code image for a redeemed offer.
     * Returns the actual PNG image file (not Base64).
     * 
     * GET /api/offers/redemption/{redemptionId}/qr-image
     * Returns: PNG image (image/png content type)
     */
    @GetMapping("/redemption/{redemptionId}/qr-image")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<?> getQRCodeImage(@PathVariable Long redemptionId) {
        try {
            // Get the redemption record to retrieve QR code data
            // This assumes you have a method in service to get redemption by ID
            // For now, returning error as we need the repository method
            
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Redemption lookup method needs to be implemented"));
            
        } catch (Exception e) {
            log.error("Error retrieving QR code image", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error retrieving QR code: " + e.getMessage()));
        }
    }
}
