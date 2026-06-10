package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {
    List<Waitlist> findByRestaurantId(Long restaurantId);

    List<Waitlist> findByRestaurantIdAndStatus(Long restaurantId, Waitlist.WaitlistStatus status);

    Optional<Waitlist> findByGuestPhone(String guestPhone);
    Optional<Waitlist> findByRestaurantIdAndGuestPhone(Long restaurantId, String guestPhone);

    @Query("SELECT w FROM Waitlist w WHERE w.restaurant.id = ?1 AND w.status IN ('WAITING', 'NOTIFIED') ORDER BY w.position ASC")
    List<Waitlist> findActiveWaitlistByRestaurant(Long restaurantId);

    @Query("SELECT COUNT(w) FROM Waitlist w WHERE w.restaurant.id = ?1 AND w.status = 'SEATED'")
    long countSeatedByRestaurant(Long restaurantId);

    List<Waitlist> findByStatus(Waitlist.WaitlistStatus status);
}

