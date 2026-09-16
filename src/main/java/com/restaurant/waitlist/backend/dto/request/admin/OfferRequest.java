package com.restaurant.waitlist.backend.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class OfferRequest {
    @NotBlank(message = "Offer name is required")
    private String name;

    @NotNull(message = "Location ID is required")
    private Long locationId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotBlank(message = "Status is required")
    private String status; // ACTIVE, INACTIVE, DRAFT

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Discount value must be greater than 0")
    private BigDecimal discountValue;

    @NotBlank(message = "Discount label is required")
    private String discountLabel; // e.g., "20% off", "$5 off"

    @NotBlank(message = "Description is required")
    private String description; // Detailed offer description

    private String photoUrl; // Optional offer image URL

    @Min(value = 1, message = "Per user limit must be at least 1")
    private Integer perUserLimit; // Max uses per guest
}
