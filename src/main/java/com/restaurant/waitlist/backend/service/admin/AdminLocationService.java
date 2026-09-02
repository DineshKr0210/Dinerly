package com.restaurant.waitlist.backend.service.admin;

import com.restaurant.waitlist.backend.dto.request.admin.LocationRequest;
import com.restaurant.waitlist.backend.dto.response.admin.LocationResponse;
import com.restaurant.waitlist.backend.dto.response.admin.LocationsPageResponse;
import org.springframework.data.domain.Pageable;

public interface AdminLocationService {
    LocationsPageResponse listLocations(Pageable pageable);
    LocationResponse createLocation(LocationRequest request);
    LocationResponse getLocation(Long id);
    LocationResponse updateLocation(Long id, LocationRequest request);
}
