package com.restaurant.waitlist.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RedeemOfferResponse {
    private Long redemptionId;
    private String redemptionCode; // 6-digit code
    private LocalDateTime expiresAt; // Code expiry time (1 hour)
    private String message; // "Show this code to your server"
    private String offerName;
    private String offerDescription;
    
    // QR Code: Base64 encoded PNG image or data URI
    // Can be displayed as: <img src="data:image/png;base64,{qrCodeDataUri}" />
    private String qrCodeDataUri; // data:image/png;base64,...
}
