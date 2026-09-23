package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.Staff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    List<Staff> findByRestaurantId(Long restaurantId);

    // Franchise-group scoped variant: an admin's "all my locations" view
    // resolves to a restaurant id list rather than a truly global query.
    Page<Staff> findByRestaurantIdIn(List<Long> restaurantIds, Pageable pageable);

    boolean existsByEmailIgnoreCase(String email);
}
