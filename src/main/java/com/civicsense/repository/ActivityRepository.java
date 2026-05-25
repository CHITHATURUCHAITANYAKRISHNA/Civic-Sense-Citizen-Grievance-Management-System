package com.civicsense.repository;

import com.civicsense.entity.Activity;
import com.civicsense.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    // Count total activities for dashboard
    long countByUser(User user);

    // Fetch all activities for a specific user (optional, if needed elsewhere)
    List<Activity> findByUser(User user);

    // Fetch latest 5 activities for dashboard
    List<Activity> findTop5ByUserOrderByTimestampDesc(User user);
}
