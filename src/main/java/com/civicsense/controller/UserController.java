package com.civicsense.controller;

import com.civicsense.entity.Activity;
import com.civicsense.entity.Issue;
import com.civicsense.entity.Notification;
import com.civicsense.entity.User;
import com.civicsense.repository.ActivityRepository;
import com.civicsense.repository.IssueRepository;
import com.civicsense.repository.NotificationRepository;
import com.civicsense.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final IssueRepository issueRepository;
    private final NotificationRepository notificationRepository;

    public UserController(UserRepository userRepository,
                          ActivityRepository activityRepository,
                          IssueRepository issueRepository,
                          NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.issueRepository = issueRepository;
        this.notificationRepository = notificationRepository;
    }

    // =========================
    // USER DASHBOARD
    // =========================
    @GetMapping("/dashboard")
    public String userDashboard(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        // User issues
        List<Issue> userIssues = issueRepository.findByUser(user, Sort.by("createdAt").descending());
        model.addAttribute("issues", userIssues);
        model.addAttribute("myIssuesCount", userIssues.size());

        // User activities
        model.addAttribute("activitiesCount", activityRepository.countByUser(user));
        List<Activity> recentActivities = activityRepository.findTop5ByUserOrderByTimestampDesc(user);
        model.addAttribute("recentActivities", recentActivities);

        // User notifications
        long notificationsCount = notificationRepository.countByUserAndReadFalse(user);
        List<Notification> notifications = notificationRepository.findTop5ByUserOrderByTimestampDesc(user);
        model.addAttribute("notificationsCount", notificationsCount);
        model.addAttribute("notifications", notifications);

        model.addAttribute("isAdmin", false);
        model.addAttribute("username", user.getUsername());

        return "user-dashboard";
    }

    // =========================
    // USER PROFILE
    // =========================
    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        model.addAttribute("dashboardUrl", "/user/dashboard");

        return "profile";
    }

    // =========================
    // ADMIN EDIT USER
    // =========================
    @GetMapping("/admin/users/edit/{id}")
    public String adminEditUser(@PathVariable("id") Long id, Model model) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (!optionalUser.isPresent()) {
            return "redirect:/admin/users?error=UserNotFound";
        }

        model.addAttribute("user", optionalUser.get());
        return "admin/admin-edit-user";
    }

    @PostMapping("/admin/users/edit/{id}")
    public String adminUpdateUser(@PathVariable("id") Long id, User updatedUser) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (!optionalUser.isPresent()) {
            return "redirect:/admin/users?error=UserNotFound";
        }

        User existingUser = optionalUser.get();
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setRole(updatedUser.getRole());

        // Update password only if provided
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(new BCryptPasswordEncoder().encode(updatedUser.getPassword()));
        }

        userRepository.save(existingUser);

        return "redirect:/admin/users?success=UserUpdated";
    }

    // =========================
    // ADMIN DELETE USER
    // =========================
    @GetMapping("/admin/users/delete/{id}")
    public String adminDeleteUser(@PathVariable("id") Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            userRepository.delete(optionalUser.get());
            return "redirect:/admin/users?success=UserDeleted";
        } else {
            return "redirect:/admin/users?error=UserNotFound";
        }
    }
}