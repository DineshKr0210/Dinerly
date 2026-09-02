package com.restaurant.waitlist.backend.dto.request.admin;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class OfferRequest {
    @NotBlank
    private String name;

    @NotNull
    private Long locationId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private String status;

    private BigDecimal value;
}
