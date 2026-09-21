package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.Staff;
import com.restaurant.waitlist.backend.entity.StaffPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StaffPermissionRepository extends JpaRepository<StaffPermission, Long> {
    Optional<StaffPermission> findByStaff(Staff staff);
    Optional<StaffPermission> findByStaffId(Long staffId);
}