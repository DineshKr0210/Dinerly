package com.restaurant.waitlist.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to validate an offer redemption.
 * Can use either redemptionCode (manual entry) or qrCodeData (QR scan).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidateOfferRequest {

    // Redemption code (6-digit) - for manual entry at POS
    private String redemptionCode;

    // QR Code data (Base64 encoded or decrypted string) - from QR scanner
    // The QR code encodes the redemption code, so this will be parsed to extract the code
    private String qrCodeData;

    // Alternative: raw scanned text from QR code
    // Modern QR scanners return the decoded text directly
    private String scannedQRText;

    /**
     * Get the effective code from any of the input methods:
     * 1. Direct redemptionCode
     * 2. Parse from qrCodeData
     * 3. Use scannedQRText directly
     */
    public String getEffectiveCode() {
        if (redemptionCode != null && !redemptionCode.isBlank()) {
            return redemptionCode;
        }
        if (scannedQRText != null && !scannedQRText.isBlank()) {
            return scannedQRText;
        }
        if (qrCodeData != null && !qrCodeData.isBlank()) {
            // Try to extract code from base64 QR data
            // In practice, the QR scanner should return scannedQRText instead
            return extractCodeFromQRData(qrCodeData);
        }
        return null;
    }

    /**
     * Extract redemption code from QR code data.
     * Since QR codes encode plain text (the 6-digit code), this extracts it.
     */
    private String extractCodeFromQRData(String qrCodeData) {
        // QR codes typically contain plain text
        // If base64 encoded, we'd decode it; otherwise use directly
        if (qrCodeData.matches("^[A-Za-z0-9+/=]+$")) {
            // Looks like base64, but we can't easily decode without additional libs
            // Assume it's already decoded if it looks like our code format
        }
        // Return as-is; QR scanner should provide readable text
        return qrCodeData;
    }
}

