package com.civicsense.service;

import com.civicsense.entity.Activity;
import com.civicsense.entity.User;
import com.civicsense.repository.ActivityRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    // Call this whenever user does something
    public void log(User user, String description) {
        if (user == null || description == null || description.isBlank()) return;

        Activity activity = new Activity();
        activity.setUser(user);
        activity.setDescription(description);
        activity.setTimestamp(LocalDateTime.now());

        activityRepository.save(activity);
    }
}
