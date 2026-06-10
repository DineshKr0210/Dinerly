package com.restaurant.waitlist.backend.controller;

import com.restaurant.waitlist.backend.dto.request.UpdateTableStatusRequest;
import com.restaurant.waitlist.backend.dto.response.ApiResponse;
import com.restaurant.waitlist.backend.service.TableService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurants/{restaurantId}/tables")
@CrossOrigin(origins = "*")
public class TableController {

    @Autowired
    private TableService tableService;

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse<Void>> updateTableStatus(
            @PathVariable Long restaurantId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateTableStatusRequest request) {
        try {
            tableService.updateTableStatus(restaurantId, id, request.getStatus());
            return ResponseEntity.ok(ApiResponse.success("Table updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}

