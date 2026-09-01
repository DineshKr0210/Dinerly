package com.restaurant.waitlist.backend.menu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequestDTO {

    @NotBlank(message = "Category name is mandatory")
    private String name;

    @NotBlank(message = "Description is mandatory")
    private String description;
}
