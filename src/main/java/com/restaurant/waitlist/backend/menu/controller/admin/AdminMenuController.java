package com.restaurant.waitlist.backend.menu.controller.admin;

import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.menu.dto.DishDTO;
import com.restaurant.waitlist.backend.menu.dto.DishRequestDTO;
import com.restaurant.waitlist.backend.menu.impl.DishServiceImpl;
import com.restaurant.waitlist.backend.menu.mapper.DishMapper;
import com.restaurant.waitlist.backend.menu.dao.DishRepository;
import com.restaurant.waitlist.backend.menu.model.Dish;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/menu")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminMenuController {

    private final DishRepository dishRepository;
    private final DishServiceImpl dishService;

    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getMenu(@RequestParam(required = false) Long locationId) {
        List<Dish> dishes = dishRepository.getAllDishesWithRelationsByRestaurant(locationId);
        List<DishDTO> dtos = dishes.stream().map(DishMapper::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Menu retrieved", dtos));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<DishDTO>> createItem(@Valid @ModelAttribute DishRequestDTO request) {
        DishDTO dto = dishService.createDish(request);
        return ResponseEntity.ok(ApiResponse.success("Menu item created successfully", dto));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<DishDTO>> updateItem(@PathVariable Long itemId, @Valid @ModelAttribute DishRequestDTO request) {
        DishDTO dto = dishService.updateDish(itemId, request);
        return ResponseEntity.ok(ApiResponse.success("Menu item updated successfully", dto));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long itemId, @RequestParam Long locationId) {
        dishService.deleteDishById(itemId, locationId);
        return ResponseEntity.ok(ApiResponse.success("Menu item deleted successfully"));
    }
}
