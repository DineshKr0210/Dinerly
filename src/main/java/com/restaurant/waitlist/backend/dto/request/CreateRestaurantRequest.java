package com.restaurant.waitlist.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class CreateRestaurantRequest {
    
    @NotBlank(message = "Restaurant name is required")
    private String name;
    
    private String address;
    
    private String phone;
    
    @Email(message = "Email should be valid")
    private String email;
    
    private Integer totalTables;
}

