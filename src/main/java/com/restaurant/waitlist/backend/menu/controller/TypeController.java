package com.restaurant.waitlist.backend.menu.controller;

import com.restaurant.waitlist.backend.menu.dto.TypeDTO;
import com.restaurant.waitlist.backend.menu.service.TypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu/types")
@RequiredArgsConstructor
public class TypeController {
    private final TypeService typeService;

    @GetMapping
    public ResponseEntity<List<TypeDTO>> getAllCategories(@RequestParam Long locationId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(typeService.getAllTypes(locationId));
    }
}
