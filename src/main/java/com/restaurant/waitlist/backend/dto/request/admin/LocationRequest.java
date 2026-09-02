package com.restaurant.waitlist.backend.dto.request.admin;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class LocationRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String address;

    @NotBlank
    @Pattern(regexp = "^\\+?[0-9 .-]{7,20}$", message = "Invalid phone number")
    private String phoneNumber;

    @Email
    private String managerEmail;

    @NotBlank
    private String ownerName;

    @Email
    private String ownerEmail;

    private String menuTemplate;

    @NotNull
    @Positive
    private Integer seats;

    @NotNull
    private Boolean locationOpen;
}
