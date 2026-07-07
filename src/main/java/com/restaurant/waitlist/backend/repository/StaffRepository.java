package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    List<Staff> findByRestaurantId(Long restaurantId);
}
