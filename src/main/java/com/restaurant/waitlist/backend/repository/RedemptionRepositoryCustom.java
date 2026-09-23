package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.Redemption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface RedemptionRepositoryCustom {
    Page<Redemption> findFiltered(Long restaurantId, LocalDateTime from, LocalDateTime to, Pageable pageable);

    // Franchise-group scoped variant: an admin's "all my locations" view
    // resolves to a restaurant id list rather than a truly global query.
    // offerId/userId are optional additional filters.
    Page<Redemption> findFiltered(List<Long> restaurantIds, LocalDateTime from, LocalDateTime to,
                                   Long offerId, Long userId, Pageable pageable);
}
