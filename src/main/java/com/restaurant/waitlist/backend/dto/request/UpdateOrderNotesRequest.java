package com.restaurant.waitlist.backend.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateOrderNotesRequest {
    @Size(max = 2000, message = "Order notes must be 2000 characters or fewer")
    private String orderNotes;
}

