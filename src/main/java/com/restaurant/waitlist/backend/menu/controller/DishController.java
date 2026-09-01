package com.restaurant.waitlist.backend.menu.controller;

import com.restaurant.waitlist.backend.menu.dto.DishDTO;
import com.restaurant.waitlist.backend.menu.dto.DishRequestDTO;
import com.restaurant.waitlist.backend.menu.service.DishService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu/dishes")
@RequiredArgsConstructor
public class DishController {
    private final DishService dishService;

    @GetMapping
    public ResponseEntity<List<DishDTO>> getAllDishes() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(dishService.getAllDishesWithRelations());
    }

    @GetMapping("/category/{categoryName}")
    public ResponseEntity<List<DishDTO>> getDishesByCategory(
            @PathVariable String categoryName) {

        List<DishDTO> dishes = dishService.getDishesByCategory(categoryName);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(dishes);
    }
    @GetMapping("/{id}")
    public ResponseEntity<List<DishDTO>> getDishesByID(
            @PathVariable Long id) {

        List<DishDTO> dishes = dishService.getDishesByID(id);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(dishes);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<DishDTO>> getDishesByType(
            @PathVariable String type) {

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(dishService.getDishesByType(type));
    }

}
