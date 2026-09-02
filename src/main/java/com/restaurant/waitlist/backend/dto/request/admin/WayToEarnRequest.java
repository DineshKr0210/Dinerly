package com.restaurant.waitlist.backend.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WayToEarnRequest {
    @NotBlank
    private String action;

    @NotNull
    private Integer points;
}
