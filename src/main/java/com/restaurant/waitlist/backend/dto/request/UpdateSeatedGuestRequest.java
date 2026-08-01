package com.restaurant.waitlist.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateSeatedGuestRequest {

    @Min(value = 1, message = "partySize must be at least 1")
    private Integer partySize;

    @Size(max = 100, message = "tableName must not exceed 100 characters")
    private String tableName;
}
