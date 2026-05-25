package com.civicsense.controller;

import com.civicsense.entity.Notification;
import com.civicsense.entity.User;
import com.civicsense.repository.NotificationRepository;
import com.civicsense.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/user/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationController(NotificationRepository notificationRepository,
                                  UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    // =========================
    // VIEW ALL NOTIFICATIONS
    // =========================
    @GetMapping
    public String viewNotifications(Authentication authentication, Model model) {
        if (authentication == null) return "redirect:/login";

        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        List<Notification> notifications = notificationRepository.findByUserOrderByTimestampDesc(user);
        model.addAttribute("notifications", notifications);

        return "notifications"; // Thymeleaf template
    }

    // =========================
    // MARK NOTIFICATION AS READ
    // =========================
    @PostMapping("/read/{id}")
    public String markAsRead(@PathVariable Long id, Authentication authentication) {
        if (authentication == null) return "redirect:/login";

        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        Notification notification = notificationRepository.findById(id).orElse(null);
        if (notification != null && notification.getUser().getId().equals(user.getId())) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }

        return "redirect:/user/notifications";
    }

    // =========================
    // DELETE NOTIFICATION
    // =========================
    @PostMapping("/delete/{id}")
    public String deleteNotification(@PathVariable Long id, Authentication authentication) {
        if (authentication == null) return "redirect:/login";

        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        Notification notification = notificationRepository.findById(id).orElse(null);
        if (notification != null && notification.getUser().getId().equals(user.getId())) {
            notificationRepository.delete(notification);
        }

        return "redirect:/user/notifications";
    }
}
