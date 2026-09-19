package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    boolean existsByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailIncludingDeleted(@Param("email") String email);

    List<User> findAllByDeletedAtIsNullOrderByCreatedAtDesc();

    // ✅ NEW: Find by staff_id
    @Query("SELECT u FROM User u WHERE u.staffId = :staffId AND u.deletedAt IS NULL")
    Optional<User> findByStaffId(@Param("staffId") Long staffId);

    // ✅ NEW: Find by restaurant and role
    @Query("SELECT u FROM User u WHERE u.restaurantId = :restaurantId AND u.role = :role AND u.deletedAt IS NULL")
    List<User> findByRestaurantIdAndRole(@Param("restaurantId") Long restaurantId, @Param("role") User.UserRole role);

    // ✅ NEW: Find all active staff in a restaurant
    @Query("SELECT u FROM User u WHERE u.restaurantId = :restaurantId AND u.role IN ('STAFF', 'HOST', 'MANAGER', 'OWNER') AND u.deletedAt IS NULL")
    List<User> findAllRestaurantStaff(@Param("restaurantId") Long restaurantId);
}

