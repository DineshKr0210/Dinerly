package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.Redemption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface RedemptionRepositoryCustom {
    Page<Redemption> findFiltered(Long restaurantId, LocalDateTime from, LocalDateTime to, Pageable pageable);
}
