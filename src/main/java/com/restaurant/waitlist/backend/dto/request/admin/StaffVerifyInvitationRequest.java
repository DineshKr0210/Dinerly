package com.restaurant.waitlist.backend.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StaffVerifyInvitationRequest {
    @NotBlank(message = "Invitation token is required")
    private String token;
}

