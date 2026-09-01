package com.restaurant.waitlist.backend.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CategoryRequestDTO {

    @NotBlank(message = "Category name is mandatory")
    private String name;

    @NotBlank(message = "Description is mandatory")
    private String description;

    @NotNull(message = "Location ID is mandatory")
    @Positive(message = "Location ID must be a positive number")
    private Long locationId;
}
