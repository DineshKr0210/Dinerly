package com.restaurant.waitlist.backend.menu.mapper;

import com.restaurant.waitlist.backend.menu.dto.DishDTO;
import com.restaurant.waitlist.backend.menu.model.Dish;

import java.util.stream.Collectors;

public class DishMapper {
    public static DishDTO toDTO(Dish dish) {
        if (dish == null) return null;
        return DishDTO.builder()
                .id(dish.getId() != null ? Long.valueOf(dish.getId()) : null)
                .name(dish.getDishName())
                .description(dish.getDescription())
                .price(dish.getPrice())
            .category(dish.getCategory() != null ? dish.getCategory().getName() : null)
            .locationId(dish.getRestaurant() != null ? dish.getRestaurant().getId() : null)
            .location(dish.getRestaurant() != null ? dish.getRestaurant().getName() : null)
                .calories(dish.getCalories()!= null ? dish.getCalories() : null)
                .imageUrl(dish.getImageUrl()!= null ? dish.getImageUrl() : null)
                .types(
                dish.getTypes().stream()
                        .map(TypeMapper::toDTO)
                        .collect(Collectors.toList())
        )
                .status(dish.getStatus())
                .build();
    }
}
