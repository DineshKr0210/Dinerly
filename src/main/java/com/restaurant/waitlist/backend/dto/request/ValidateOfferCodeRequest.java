package com.restaurant.waitlist.backend.dto.request;

import lombok.Data;

@Data
public class ValidateOfferCodeRequest {
    // Optional: the text decoded from scanning the offer's QR code on a phone/tablet
    // camera. When present it's used instead of the {code} path variable, so the same
    // endpoint works for both manual code entry and QR scanning.
    private String qrCode;
}
