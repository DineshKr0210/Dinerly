package com.restaurant.waitlist.backend.dto.request;

import com.restaurant.waitlist.backend.entity.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateTableStatusRequest {
    @NotNull(message = "Status is required")
    private Table.TableStatus status;
}

