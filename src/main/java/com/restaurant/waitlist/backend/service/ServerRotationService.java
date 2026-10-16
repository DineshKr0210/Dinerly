package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.dto.response.ServerFloorStatusResponse;
import com.restaurant.waitlist.backend.entity.ServerFloorStatus;
import com.restaurant.waitlist.backend.entity.Staff;
import com.restaurant.waitlist.backend.entity.StaffRole;
import com.restaurant.waitlist.backend.repository.RestaurantRepository;
import com.restaurant.waitlist.backend.repository.ServerFloorStatusRepository;
import com.restaurant.waitlist.backend.repository.StaffRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implements the "Server Rotation Logic" fairness algorithm:
 * - Servers "on the floor" are kept in a rotation order, each with a running seated-count.
 * - Exactly one on-floor server is marked "Next Up".
 * - The Next-Up pointer only advances when the party is actually seated with the Next-Up
 *   server. Seating a party with a different (out-of-turn) server leaves Next-Up unchanged -
 *   they remain first in line for the following seating. The pointer never silently skips anyone.
 * - A manager can explicitly "start a new shift", which resets seated-counts to zero
 *   (automatic/date-based resets are NOT used - this is a deliberate, manual action).
 */
@Service
public class ServerRotationService {

    private static final Logger log = LoggerFactory.getLogger(ServerRotationService.class);

    @Autowired
    private ServerFloorStatusRepository floorStatusRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    // ---------------------------------------------------------------------
    // Roster / floor management
    // ---------------------------------------------------------------------

    public List<ServerFloorStatusResponse> getFloorRoster(Long restaurantId) {
        restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        List<ServerFloorStatus> rows = floorStatusRepository.findByRestaurantIdOrderByRotationOrderAsc(restaurantId);
        return rows.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ServerFloorStatusResponse toResponse(ServerFloorStatus status) {
        Staff staff = staffRepository.findById(status.getStaffId()).orElse(null);
        return ServerFloorStatusResponse.builder()
                .staffId(status.getStaffId())
                .name(staff != null ? staff.getName() : null)
                .onFloor(status.getOnFloor())
                .seatedCount(status.getSeatedCount())
                .rotationOrder(status.getRotationOrder())
                .nextUp(status.getIsNextUp())
                .build();
    }

    @Transactional
    public List<ServerFloorStatusResponse> setOnFloor(Long restaurantId, Long staffId, boolean onFloor) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff member not found"));
        if (staff.getRestaurant() == null || !staff.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("Staff member does not belong to the specified restaurant");
        }
        if (staff.getRole() != StaffRole.SERVER) {
            throw new RuntimeException("Only staff with role SERVER can be placed on the floor rotation");
        }

        ServerFloorStatus status = floorStatusRepository.findByStaffId(staffId)
                .orElseGet(() -> ServerFloorStatus.builder()
                        .restaurantId(restaurantId)
                        .staffId(staffId)
                        .onFloor(false)
                        .seatedCount(0)
                        .rotationOrder(0)
                        .isNextUp(false)
                        .build());

        if (onFloor) {
            addToRotation(restaurantId, status);
        } else {
            removeFromRotation(restaurantId, status);
        }

        return getFloorRoster(restaurantId);
    }

    private void addToRotation(Long restaurantId, ServerFloorStatus status) {
        if (Boolean.TRUE.equals(status.getOnFloor())) {
            return; // already on the floor, no-op
        }
        List<ServerFloorStatus> currentlyOnFloor = floorStatusRepository
                .findByRestaurantIdAndOnFloorTrueOrderByRotationOrderAsc(restaurantId);

        int maxOrder = currentlyOnFloor.stream()
                .map(ServerFloorStatus::getRotationOrder)
                .max(Integer::compareTo)
                .orElse(0);

        status.setOnFloor(true);
        status.setRotationOrder(maxOrder + 1);
        status.setShiftStartedAt(LocalDateTime.now());
        // New arrivals keep a (reset) seated count of 0 for this stint on the floor.
        status.setSeatedCount(0);

        if (currentlyOnFloor.isEmpty()) {
            // First server on the floor automatically becomes Next Up.
            status.setIsNextUp(true);
        } else {
            status.setIsNextUp(false);
        }

        floorStatusRepository.save(status);
    }

    private void removeFromRotation(Long restaurantId, ServerFloorStatus status) {
        if (!Boolean.TRUE.equals(status.getOnFloor())) {
            status.setOnFloor(false);
            floorStatusRepository.save(status);
            return; // already off the floor
        }

        boolean wasNextUp = Boolean.TRUE.equals(status.getIsNextUp());

        status.setOnFloor(false);
        status.setIsNextUp(false);
        floorStatusRepository.save(status);

        if (wasNextUp) {
            // Advance the pointer to whoever is still on the floor, in rotation order.
            List<ServerFloorStatus> remaining = floorStatusRepository
                    .findByRestaurantIdAndOnFloorTrueOrderByRotationOrderAsc(restaurantId);
            if (!remaining.isEmpty()) {
                ServerFloorStatus newNextUp = remaining.get(0);
                newNextUp.setIsNextUp(true);
                floorStatusRepository.save(newNextUp);
            }
        }
    }

    /**
     * Manager-initiated "start a new shift": resets seated-counts to zero for everyone
     * currently on the floor and normalizes the rotation order / Next-Up pointer to start
     * fresh from the first server in line. Off-floor servers are left untouched.
     * This is an explicit action only - there is no automatic/date-based reset.
     */
    @Transactional
    public List<ServerFloorStatusResponse> startNewShift(Long restaurantId) {
        restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        List<ServerFloorStatus> onFloor = floorStatusRepository
                .findByRestaurantIdAndOnFloorTrueOrderByRotationOrderAsc(restaurantId);

        LocalDateTime now = LocalDateTime.now();
        int order = 1;
        for (ServerFloorStatus status : onFloor) {
            status.setSeatedCount(0);
            status.setRotationOrder(order++);
            status.setIsNextUp(false);
            status.setShiftStartedAt(now);
        }
        if (!onFloor.isEmpty()) {
            onFloor.get(0).setIsNextUp(true);
        }
        floorStatusRepository.saveAll(onFloor);

        log.info("New shift started for restaurantId={}, {} server(s) on the floor", restaurantId, onFloor.size());
        return getFloorRoster(restaurantId);
    }

    // ---------------------------------------------------------------------
    // Seating assignment
    // ---------------------------------------------------------------------

    /**
     * Resolves which server should be credited with a new seating event and applies the
     * rotation rule. Returns the assigned Staff.id, or null if no server is on the floor.
     *
     * @param restaurantId      the restaurant
     * @param requestedServerId optional explicit server chosen by the host; null = auto (Next Up)
     */
    @Transactional
    public Long assignServerForSeating(Long restaurantId, Long requestedServerId) {
        List<ServerFloorStatus> onFloor = floorStatusRepository
                .findByRestaurantIdAndOnFloorTrueOrderByRotationOrderAsc(restaurantId);

        if (onFloor.isEmpty()) {
            log.warn("assignServerForSeating | no servers on the floor for restaurantId={}", restaurantId);
            return null;
        }

        Optional<ServerFloorStatus> nextUpOpt = onFloor.stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsNextUp()))
                .findFirst();
        ServerFloorStatus nextUp = nextUpOpt.orElse(onFloor.get(0));

        ServerFloorStatus target;
        if (requestedServerId == null) {
            target = nextUp;
        } else {
            target = onFloor.stream()
                    .filter(s -> s.getStaffId().equals(requestedServerId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Selected server is not currently on the floor"));
        }

        target.setSeatedCount(target.getSeatedCount() == null ? 1 : target.getSeatedCount() + 1);

        if (target.getStaffId().equals(nextUp.getStaffId())) {
            // In-turn seating: advance the pointer to the next server in the rotation.
            nextUp.setIsNextUp(false);
            ServerFloorStatus following = findNextInRotation(onFloor, nextUp);
            following.setIsNextUp(true);
            if (!following.getStaffId().equals(nextUp.getStaffId())) {
                floorStatusRepository.save(following);
            }
        }
        // Out-of-turn seating: Next-Up pointer stays exactly where it was.

        floorStatusRepository.save(target);
        if (!target.getStaffId().equals(nextUp.getStaffId())) {
            floorStatusRepository.save(nextUp);
        }

        return target.getStaffId();
    }

    private ServerFloorStatus findNextInRotation(List<ServerFloorStatus> onFloorOrdered, ServerFloorStatus current) {
        int idx = onFloorOrdered.indexOf(current);
        if (idx < 0 || onFloorOrdered.size() == 1) {
            return current;
        }
        int nextIdx = (idx + 1) % onFloorOrdered.size();
        return onFloorOrdered.get(nextIdx);
    }
}


