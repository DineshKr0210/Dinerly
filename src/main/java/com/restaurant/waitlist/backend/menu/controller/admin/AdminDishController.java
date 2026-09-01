package com.restaurant.waitlist.backend.menu.controller.admin;

import com.restaurant.waitlist.backend.menu.dto.DishDTO;
import com.restaurant.waitlist.backend.menu.dto.DishRequestDTO;
import com.restaurant.waitlist.backend.menu.service.DishService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu/admin/dishes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDishController {
    private final DishService dishService;

    @GetMapping("/category/{categoryName}")
    public ResponseEntity<List<DishDTO>> getDishesByCategory(
            @PathVariable String categoryName) {

        List<DishDTO> dishes = dishService.getDishesByCategoryAdmin(categoryName);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(dishes);
    }
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DishDTO> createDish(@Valid @ModelAttribute DishRequestDTO dto) {
        return ResponseEntity.ok(dishService.createDish(dto));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DishDTO> updateDish(@PathVariable Long id,
                                              @Valid @ModelAttribute DishRequestDTO dto) {
        return ResponseEntity.ok(dishService.updateDish(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDish(@PathVariable Long id) {
        dishService.deleteDishById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<Void> restore(@PathVariable Long id) {
        dishService.restoreDishById(id);
        return ResponseEntity.noContent().build();
    }
}
