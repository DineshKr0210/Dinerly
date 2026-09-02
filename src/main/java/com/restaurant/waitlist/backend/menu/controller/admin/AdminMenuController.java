package com.restaurant.waitlist.backend.menu.controller.admin;

import com.restaurant.waitlist.backend.menu.dto.CategoryDTO;
import com.restaurant.waitlist.backend.menu.dto.CategoryRequestDTO;
import com.restaurant.waitlist.backend.menu.dto.DishDTO;
import com.restaurant.waitlist.backend.menu.dto.DishRequestDTO;
import com.restaurant.waitlist.backend.menu.service.CategoryService;
import com.restaurant.waitlist.backend.menu.service.DishService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/menu")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminMenuController {

    private final CategoryService categoryService;
    private final DishService dishService;

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDTO>> getAllCategories(@RequestParam Long locationId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(categoryService.getAllCategoriesadmin(locationId));
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryRequestDTO dto) {
        return ResponseEntity.ok(categoryService.createCategory(dto));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id,
                                                     @Valid @RequestBody CategoryRequestDTO dto) {
        return ResponseEntity.ok(categoryService.updateCategory(id, dto));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id, @RequestParam Long locationId) {
        categoryService.deleteCategoryById(id, locationId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/categories/{id}/restore")
    public ResponseEntity<Void> restoreCategory(@PathVariable Long id, @RequestParam Long locationId) {
        categoryService.restoreCategoryById(id, locationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dishes/category/{categoryName}")
    public ResponseEntity<List<DishDTO>> getDishesByCategory(@PathVariable String categoryName,
                                                            @RequestParam Long locationId) {
        List<DishDTO> dishes = dishService.getDishesByCategoryAdmin(categoryName, locationId);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(dishes);
    }

    @PostMapping(value = "/dishes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DishDTO> createDish(@Valid @ModelAttribute DishRequestDTO dto) {
        return ResponseEntity.ok(dishService.createDish(dto));
    }

    @PutMapping(value = "/dishes/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DishDTO> updateDish(@PathVariable Long id,
                                            @Valid @ModelAttribute DishRequestDTO dto) {
        return ResponseEntity.ok(dishService.updateDish(id, dto));
    }

    @DeleteMapping("/dishes/{id}")
    public ResponseEntity<Void> deleteDish(@PathVariable Long id, @RequestParam Long locationId) {
        dishService.deleteDishById(id, locationId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/dishes/{id}/restore")
    public ResponseEntity<Void> restoreDish(@PathVariable Long id, @RequestParam Long locationId) {
        dishService.restoreDishById(id, locationId);
        return ResponseEntity.noContent().build();
    }
}
