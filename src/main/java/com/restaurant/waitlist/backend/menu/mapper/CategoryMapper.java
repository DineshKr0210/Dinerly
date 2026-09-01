package com.restaurant.waitlist.backend.menu.mapper;

import com.restaurant.waitlist.backend.menu.dto.CategoryDTO;
import com.restaurant.waitlist.backend.menu.model.Category;

import java.util.stream.Collectors;

public class CategoryMapper {

    public static CategoryDTO toDTO(Category category) {
        if (category == null) return null;
        return CategoryDTO.builder()
                .id(category.getId() != null ? Long.valueOf(category.getId()) : null)
                .name(category.getName())
                .description(category.getDescription())
                .locationId(category.getRestaurant() != null ? category.getRestaurant().getId() : null)
                .location(category.getRestaurant() != null ? category.getRestaurant().getName() : null)
                .dishes(category.getDishes() != null ?
                        category.getDishes().stream()
                                .map(DishMapper::toDTO)
                                .collect(Collectors.toList())
                        : null)
                .status(category.getStatus())
                .build();
    }

    public static CategoryDTO catToDTO(Category category) {
        if (category == null) return null;
        return CategoryDTO.builder()
                .id(Long.valueOf(category.getId() != null ? category.getId() : null))
                .name(category.getName())
                .locationId(category.getRestaurant() != null ? category.getRestaurant().getId() : null)
                .location(category.getRestaurant() != null ? category.getRestaurant().getName() : null)
                .status(category.getStatus())
                .build();
    }
}
