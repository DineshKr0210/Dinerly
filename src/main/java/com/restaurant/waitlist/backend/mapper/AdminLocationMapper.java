package com.restaurant.waitlist.backend.mapper;

import com.restaurant.waitlist.backend.dto.response.admin.LocationResponse;
import com.restaurant.waitlist.backend.entity.Restaurant;

public class AdminLocationMapper {

    public static LocationResponse toResponse(Restaurant r) {
        if (r == null) return null;
        return LocationResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .address(r.getAddress())
                .phoneNumber(r.getPhone())
                .managerEmail(r.getManagerEmail())
                .ownerName(r.getOwnerName())
                .ownerEmail(r.getOwnerEmail())
                .menuTemplate(r.getMenuTemplate())
                .seats(r.getSeats())
                .locationOpen(r.getLocationOpen())
                .build();
    }
}
