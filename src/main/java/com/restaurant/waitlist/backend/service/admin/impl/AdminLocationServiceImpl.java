package com.restaurant.waitlist.backend.service.admin.impl;

import com.restaurant.waitlist.backend.dto.request.admin.LocationRequest;
import com.restaurant.waitlist.backend.dto.response.admin.LocationResponse;
import com.restaurant.waitlist.backend.dto.response.admin.LocationsPageResponse;
import com.restaurant.waitlist.backend.entity.AuditLog;
import com.restaurant.waitlist.backend.entity.Restaurant;
import com.restaurant.waitlist.backend.mapper.AdminLocationMapper;
import com.restaurant.waitlist.backend.repository.AuditLogRepository;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.service.admin.AdminLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminLocationServiceImpl implements AdminLocationService {

    private final RestaurantRepository restaurantRepository;
    private final AuditLogRepository auditLogRepository;

    @Override
    public LocationsPageResponse listLocations(Pageable pageable) {
        Page<Restaurant> page = restaurantRepository.findAll(pageable);
        return LocationsPageResponse.builder()
                .locations(page.stream().map(AdminLocationMapper::toResponse).toList())
                .pagination(LocationsPageResponse.Pagination.builder()
                        .page(page.getNumber())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public LocationResponse createLocation(LocationRequest request) {
        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .address(request.getAddress())
                .phone(request.getPhoneNumber())
                .managerEmail(request.getManagerEmail())
                .ownerName(request.getOwnerName())
                .ownerEmail(request.getOwnerEmail())
                .menuTemplate(request.getMenuTemplate())
                .seats(request.getSeats())
                .locationOpen(request.getLocationOpen())
                .build();

        Restaurant saved = restaurantRepository.save(restaurant);

        // audit
        AuditLog log = AuditLog.builder()
                .restaurantId(saved.getId())
                .action("LOCATION_CREATED")
                .details("Location created: " + saved.getName())
                .build();
        auditLogRepository.save(log);

        return AdminLocationMapper.toResponse(saved);
    }

    @Override
    public LocationResponse getLocation(Long id) {
        return restaurantRepository.findById(id).map(AdminLocationMapper::toResponse).orElse(null);
    }

    @Override
    @Transactional
    public LocationResponse updateLocation(Long id, LocationRequest request) {
        Restaurant r = restaurantRepository.findById(id).orElseThrow(() -> new RuntimeException("Location not found"));
        r.setName(request.getName());
        r.setAddress(request.getAddress());
        r.setPhone(request.getPhoneNumber());
        r.setManagerEmail(request.getManagerEmail());
        r.setOwnerName(request.getOwnerName());
        r.setOwnerEmail(request.getOwnerEmail());
        r.setMenuTemplate(request.getMenuTemplate());
        r.setSeats(request.getSeats());
        r.setLocationOpen(request.getLocationOpen());

        Restaurant saved = restaurantRepository.save(r);

        AuditLog log = AuditLog.builder()
                .restaurantId(saved.getId())
                .action("LOCATION_UPDATED")
                .details("Location updated: " + saved.getName())
                .build();
        auditLogRepository.save(log);

        return AdminLocationMapper.toResponse(saved);
    }
}
