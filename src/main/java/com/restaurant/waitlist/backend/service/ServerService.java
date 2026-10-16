package com.restaurant.waitlist.backend.service;

import com.restaurant.waitlist.backend.dto.response.ServerAssignedTableResponse;
import com.restaurant.waitlist.backend.dto.response.ServerNotificationResponse;
import com.restaurant.waitlist.backend.entity.ServerNotification;
import com.restaurant.waitlist.backend.entity.Staff;
import com.restaurant.waitlist.backend.entity.Waitlist;
import com.restaurant.waitlist.backend.repository.ServerNotificationRepository;
import com.restaurant.waitlist.backend.repository.StaffRepository;
import com.restaurant.waitlist.backend.repository.WaitlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Backs the dedicated server-facing UI screen: "which tables are mine right now" and
 * "what notifications (new table assignments) do I have".
 */
@Service
public class ServerService {

    @Autowired
    private WaitlistRepository waitlistRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private ServerNotificationRepository serverNotificationRepository;

    /**
     * Resolves the Staff.id for the currently authenticated server user and validates
     * that they belong to the restaurant being queried.
     */
    public Long resolveStaffId(Long restaurantId, Long staffId) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff member not found"));
        if (staff.getRestaurant() == null || !staff.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("Staff member does not belong to the specified restaurant");
        }
        return staff.getId();
    }

    public List<ServerAssignedTableResponse> getMyTables(Long restaurantId, Long staffId) {
        List<Waitlist> seated = waitlistRepository.findByRestaurantIdAndAssignedServerIdAndStatusOrderBySeatedAtDesc(
                restaurantId, staffId, Waitlist.WaitlistStatus.SEATED);
        return seated.stream()
                .map(ServerAssignedTableResponse::fromWaitlist)
                .collect(Collectors.toList());
    }

    public Page<ServerNotificationResponse> getMyNotifications(Long restaurantId, Long staffId, Pageable pageable) {
        Page<ServerNotification> page = serverNotificationRepository
                .findByRestaurantIdAndStaffIdOrderByCreatedAtDesc(restaurantId, staffId, pageable);
        return page.map(ServerNotificationResponse::fromEntity);
    }

    public long getUnreadNotificationCount(Long restaurantId, Long staffId) {
        return serverNotificationRepository.countByRestaurantIdAndStaffIdAndIsReadFalse(restaurantId, staffId);
    }

    @Transactional
    public void markNotificationRead(Long restaurantId, Long staffId, Long notificationId) {
        ServerNotification notification = serverNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        if (!notification.getRestaurantId().equals(restaurantId) || !notification.getStaffId().equals(staffId)) {
            throw new RuntimeException("Notification does not belong to this server");
        }
        notification.setIsRead(true);
        serverNotificationRepository.save(notification);
    }

    @Transactional
    public void notifyTableAssigned(Long restaurantId, Long staffId, Long waitlistId, Long tableId, String message) {
        ServerNotification notification = ServerNotification.builder()
                .restaurantId(restaurantId)
                .staffId(staffId)
                .waitlistId(waitlistId)
                .tableId(tableId)
                .type(ServerNotification.NotificationType.TABLE_ASSIGNED)
                .message(message)
                .isRead(false)
                .build();
        serverNotificationRepository.save(notification);
    }

    /**
     * Lets a server jot down free-text notes about what the table/guest wants (order
     * items, special requests, allergies mentioned tableside, etc.). Restricted to the
     * server the party is actually assigned to, and only while they're still seated.
     */
    @Transactional
    public ServerAssignedTableResponse updateOrderNotes(Long restaurantId, Long staffId, Long waitlistId, String orderNotes) {
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new RuntimeException("Waitlist entry not found"));

        if (waitlist.getRestaurant() == null || !waitlist.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("Waitlist entry does not belong to the specified restaurant");
        }
        if (waitlist.getAssignedServerId() == null || !waitlist.getAssignedServerId().equals(staffId)) {
            throw new RuntimeException("You can only add order notes to tables assigned to you");
        }
        if (waitlist.getStatus() != Waitlist.WaitlistStatus.SEATED) {
            throw new RuntimeException("Order notes can only be added while the party is seated");
        }

        waitlist.setOrderNotes(orderNotes);
        waitlist.setOrderNotesUpdatedAt(java.time.LocalDateTime.now());
        waitlistRepository.save(waitlist);

        return ServerAssignedTableResponse.fromWaitlist(waitlist);
    }
}

