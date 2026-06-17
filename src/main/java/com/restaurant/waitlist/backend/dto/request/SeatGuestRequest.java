package com.restaurant.waitlist.backend.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SeatGuestRequest {
    @Size(max = 100)
    private String tableName;
}

