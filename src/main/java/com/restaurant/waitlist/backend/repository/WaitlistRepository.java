package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

@Repository
public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {
    List<Waitlist> findByRestaurantId(Long restaurantId);

    List<Waitlist> findByRestaurantIdAndStatus(Long restaurantId, Waitlist.WaitlistStatus status);

    Optional<Waitlist> findByGuestPhone(String guestPhone);

    // Return the most recent waitlist entry (by joinedAt desc, then by id desc) for a given restaurant and guest phone
    @Query("SELECT w FROM Waitlist w WHERE w.restaurant.id = :restaurantId AND w.guestPhone = :guestPhone ORDER BY w.id DESC, w.id DESC LIMIT 1")
    Optional<Waitlist> findLatestByRestaurantIdAndGuestPhone(@Param("restaurantId") Long restaurantId, @Param("guestPhone") String guestPhone);

    @Query("SELECT w FROM Waitlist w WHERE w.restaurant.id = :restaurantId AND w.guestPhone = :guestPhone ORDER BY w.id DESC")
    List<Waitlist> findAllByRestaurantIdAndGuestPhone(@Param("restaurantId") Long restaurantId, @Param("guestPhone") String guestPhone);

    @Query("SELECT w FROM Waitlist w WHERE w.restaurant.id = ?1 AND w.status IN ('WAITING', 'NOTIFIED') ORDER BY w.position ASC")
    List<Waitlist> findActiveWaitlistByRestaurant(Long restaurantId);

    @Query("SELECT COUNT(w) FROM Waitlist w WHERE w.restaurant.id = ?1 AND w.status = 'SEATED'")
    long countSeatedByRestaurant(Long restaurantId);

    List<Waitlist> findByStatus(Waitlist.WaitlistStatus status);
}

