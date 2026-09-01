package com.restaurant.waitlist.backend.menu.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class DishRequestDTO {

    @NotBlank(message = "Dish name is mandatory")
    private String dishName;

    @NotBlank(message = "Category name is mandatory")
    private String categoryName;

    @NotNull(message = "Types are mandatory")
    @Size(min = 1, message = "At least one type is required")
    private List<@NotBlank String> typeNames;

    @NotNull(message = "Price is mandatory")
    @DecimalMin(value = "0.1", message = "Price must be greater than zero")
    private Double price;

    @Min(value = 1, message = "Calories must be greater than zero")
    private Integer calories;

    @NotBlank(message = "Description is mandatory")
    private String description;

    @NotNull(message = "Image is mandatory")
    private MultipartFile image;
}
