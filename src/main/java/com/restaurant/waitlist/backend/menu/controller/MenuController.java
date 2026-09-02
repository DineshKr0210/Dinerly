package com.restaurant.waitlist.backend.menu.controller;

import com.restaurant.waitlist.backend.menu.dto.CategoryDTO;
import com.restaurant.waitlist.backend.menu.dto.DishDTO;
import com.restaurant.waitlist.backend.menu.dto.TypeDTO;
import com.restaurant.waitlist.backend.menu.service.CategoryService;
import com.restaurant.waitlist.backend.menu.service.DishService;
import com.restaurant.waitlist.backend.menu.service.TypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final CategoryService categoryService;
    private final DishService dishService;
    private final TypeService typeService;

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDTO>> getAllCategories(@RequestParam Long locationId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(categoryService.getAllCategories(locationId));
    }

    @GetMapping("/categories/with-dishes")
    public ResponseEntity<List<CategoryDTO>> getAllCategoriesWithDishes(@RequestParam Long locationId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(categoryService.getAllCategoriesWithDishes(locationId));
    }

    @GetMapping("/categories/{id}/dishes")
    public ResponseEntity<CategoryDTO> getCategoryWithDishes(@PathVariable Long id, @RequestParam Long locationId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(categoryService.getCategoryWithDishes(id, locationId));
    }

    @GetMapping("/dishes")
    public ResponseEntity<List<DishDTO>> getAllDishes(@RequestParam Long locationId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(dishService.getAllDishesWithRelations(locationId));
    }

    @GetMapping("/dishes/category/{categoryName}")
    public ResponseEntity<List<DishDTO>> getDishesByCategory(@PathVariable String categoryName) {
        List<DishDTO> dishes = dishService.getDishesByCategory(categoryName, null);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(dishes);
    }

    @GetMapping("/dishes/{id}")
    public ResponseEntity<List<DishDTO>> getDishesByID(@PathVariable Long id) {
        List<DishDTO> dishes = dishService.getDishesByID(id, null);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(dishes);
    }

    @GetMapping("/dishes/type/{type}")
    public ResponseEntity<List<DishDTO>> getDishesByType(@PathVariable String type) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(dishService.getDishesByType(type, null));
    }

    @GetMapping("/types")
    public ResponseEntity<List<TypeDTO>> getAllTypes(@RequestParam Long locationId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(typeService.getAllTypes(locationId));
    }
}
