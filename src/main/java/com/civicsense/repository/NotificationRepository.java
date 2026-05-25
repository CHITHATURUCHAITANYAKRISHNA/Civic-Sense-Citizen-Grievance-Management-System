package com.civicsense.repository;

import com.civicsense.entity.Notification;
import com.civicsense.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Fetch all notifications for a specific user
    List<Notification> findByUser(User user);

    // Fetch all notifications sorted by latest first (FOR FULL PAGE)
    List<Notification> findByUserOrderByTimestampDesc(User user);

    // Fetch only unread notifications
    List<Notification> findByUserAndReadFalse(User user);

    // Count unread notifications
    long countByUserAndReadFalse(User user);

    // Fetch latest 5 notifications (FOR DASHBOARD)
    List<Notification> findTop5ByUserOrderByTimestampDesc(User user);
}
