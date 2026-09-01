package com.restaurant.waitlist.backend.menu.controller;

import com.restaurant.waitlist.backend.menu.dto.CategoryDTO;
import com.restaurant.waitlist.backend.menu.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories(@RequestParam Long locationId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(categoryService.getAllCategories(locationId));
    }

    @GetMapping("/with-dishes")
    public ResponseEntity<List<CategoryDTO>> getAllCategoriesWithDishes(@RequestParam Long locationId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(categoryService.getAllCategoriesWithDishes(locationId));
    }

    @GetMapping("/{id}/dishes")
    public ResponseEntity<CategoryDTO> getCategoryWithDishes(@PathVariable Long id, @RequestParam Long locationId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(categoryService.getCategoryWithDishes(id, locationId));
    }

}
