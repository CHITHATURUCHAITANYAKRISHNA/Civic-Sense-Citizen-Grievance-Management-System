package com.civicsense.controller;

import com.civicsense.entity.Category;
import com.civicsense.entity.Issue;
import com.civicsense.entity.Notification;
import com.civicsense.entity.Status;
import com.civicsense.entity.User;
import com.civicsense.repository.DepartmentRepository;
import com.civicsense.repository.IssueRepository;
import com.civicsense.repository.NotificationRepository;
import com.civicsense.repository.UserRepository;
import com.civicsense.service.ActivityService;
import com.civicsense.service.EmailService;
import com.civicsense.service.NotificationHelper;
import com.civicsense.service.SMSService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Controller
@RequestMapping("/issues")
public class IssueController {

    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final ActivityService activityService;
    private final DepartmentRepository departmentRepository;

    // =========================
    // ✅ New Autowired Services
    // =========================
    @Autowired
    private EmailService emailService;

    @Autowired
    private SMSService smsService;

    @Autowired
    private NotificationHelper notificationHelper;

    public IssueController(IssueRepository issueRepository,
                           UserRepository userRepository,
                           NotificationRepository notificationRepository,
                           ActivityService activityService,
                           DepartmentRepository departmentRepository) {
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.activityService = activityService;
        this.departmentRepository = departmentRepository;
    }

    // =========================
    // INIT BINDER FOR ENUMS
    // =========================
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Category.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                try {
                    setValue(text == null || text.isBlank() ? null :
                            Category.valueOf(text.toUpperCase()));
                } catch (Exception e) {
                    setValue(null);
                }
            }
        });

        binder.registerCustomEditor(Status.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                try {
                    setValue(text == null || text.isBlank() ?
                            Status.PENDING :
                            Status.valueOf(text.toUpperCase()));
                } catch (Exception e) {
                    setValue(Status.PENDING);
                }
            }
        });
    }

    // =========================
    // LIST ISSUES WITH FILTER & PAGINATION
    // =========================
    @GetMapping
    public String listIssues(Model model,
                             Authentication authentication,
                             @RequestParam(value = "category", required = false) Category category,
                             @RequestParam(value = "status", required = false) Status status,
                             @RequestParam(value = "page", defaultValue = "0") int page) {

        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";

        boolean isAdmin = currentUser.getRole().toUpperCase().contains("ADMIN");

        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "id"));
        Page<Issue> issuePage;

        if (isAdmin) {
            if (category != null && status != null) {
                issuePage = issueRepository.findByCategoryAndStatus(category, status, pageable);
            } else if (category != null) {
                issuePage = issueRepository.findByCategory(category, pageable);
            } else if (status != null) {
                issuePage = issueRepository.findByStatus(status, pageable);
            } else {
                issuePage = issueRepository.findAll(pageable);
            }
        } else {
            if (category != null && status != null) {
                issuePage = issueRepository.findByUserAndCategoryAndStatus(currentUser, category, status, pageable);
            } else if (category != null) {
                issuePage = issueRepository.findByUserAndCategory(currentUser, category, pageable);
            } else if (status != null) {
                issuePage = issueRepository.findByUserAndStatus(currentUser, status, pageable);
            } else {
                issuePage = issueRepository.findByUser(currentUser, pageable);
            }
        }

        model.addAttribute("issues", issuePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", issuePage.getTotalPages());
        model.addAttribute("isAdmin", isAdmin);

        // Needed for filters
        model.addAttribute("categories", Category.values());
        model.addAttribute("statuses", Status.values());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedStatus", status);

        return "issues/list";
    }

    // =========================
    // NEW ISSUE FORM
    // =========================
    @GetMapping("/new")
    public String newIssueForm(Authentication authentication, Model model) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";

        model.addAttribute("issue", new Issue());
        model.addAttribute("categories", Category.values());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("isAdmin", currentUser.getRole().toUpperCase().contains("ADMIN"));

        return "issues/new";
    }

    // =========================
    // SAVE ISSUE
    // =========================
    @PostMapping("/save")
    public String saveIssue(@ModelAttribute Issue issue,
                            @RequestParam(value = "departmentId", required = false) Long departmentId,
                            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                            @RequestParam(value = "mediaFiles", required = false) MultipartFile[] mediaFiles,
                            @RequestParam(value = "latitude", required = false) Double latitude,
                            @RequestParam(value = "longitude", required = false) Double longitude,
                            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";

        issue.setUser(currentUser);
        issue.setStatus(Status.PENDING);

        if (departmentId != null) {
            departmentRepository.findById(departmentId).ifPresent(issue::setDepartment);
        }

        issue.setLatitude(latitude);
        issue.setLongitude(longitude);

        try {
            Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads/issues");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            if (imageFile != null && !imageFile.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
                Path filePath = uploadDir.resolve(fileName);
                imageFile.transferTo(filePath.toFile());
                issue.setImagePath("/uploads/issues/" + fileName);
            }

            if (mediaFiles != null && mediaFiles.length > 0) {
                StringBuilder imagePaths = new StringBuilder();
                for (MultipartFile file : mediaFiles) {
                    if (!file.isEmpty()) {
                        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                        Path filePath = uploadDir.resolve(fileName);
                        file.transferTo(filePath.toFile());

                        String contentType = file.getContentType();
                        if (contentType != null && contentType.startsWith("video")) {
                            issue.setVideoPath("/uploads/issues/" + fileName);
                        } else {
                            imagePaths.append("/uploads/issues/").append(fileName).append(",");
                        }
                    }
                }
                if (imagePaths.length() > 0) {
                    imagePaths.setLength(imagePaths.length() - 1);
                    issue.setImagePaths(imagePaths.toString());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        issueRepository.save(issue);

        activityService.log(currentUser, "Reported new issue: " + issue.getTitle());

        // Notify admins
        List<User> admins = userRepository.findByRoleContainingIgnoreCase("ADMIN");
        for (User admin : admins) {
            Notification notification = new Notification();
            notification.setUser(admin);
            notification.setMessage("New issue submitted: " + issue.getTitle());
            notification.setRead(false);
            notificationRepository.save(notification);
        }

        // Notify user
        Notification userNotif = new Notification();
        userNotif.setUser(currentUser);
        userNotif.setMessage("Your issue '" + issue.getTitle() + "' has been submitted successfully.");
        userNotif.setRead(false);
        notificationRepository.save(userNotif);

        return "redirect:/issues";
    }

    // =========================
    // VIEW ISSUE
    // =========================
    @GetMapping("/{id}")
    public String viewIssue(@PathVariable Long id, Model model, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";

        Issue issue = issueRepository.findById(id).orElse(null);
        if (issue == null) return "redirect:/issues";

        boolean isAdmin = currentUser.getRole().toUpperCase().contains("ADMIN");

        if (!isAdmin && !issue.getUser().getId().equals(currentUser.getId()))
            return "redirect:/issues";

        model.addAttribute("issue", issue);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("departments", departmentRepository.findAll());

        return "issues/details";
    }

    // =========================
    // ✅ NEW METHOD: UPDATE ISSUE STATUS (ADMIN)
    // =========================
    @PostMapping("/update-status")
    public String updateIssueStatus(@RequestParam Long issueId,
                                    @RequestParam Status newStatus,
                                    Authentication authentication) {

        User admin = getCurrentUser(authentication);
        if (admin == null) return "redirect:/login";

        Issue issue = issueRepository.findById(issueId).orElse(null);
        if (issue == null) return "redirect:/issues";

        // Update status
        issue.setStatus(newStatus);
        issueRepository.save(issue);

        activityService.log(admin, "Updated status for issue #" + issueId + " to " + newStatus);

        // ✅ Send Email + SMS notifications safely
        notificationHelper.notifyUserStatusChange(issue);

        return "redirect:/admin/admin-update-status?issueId=" + issueId;
    }

    // =========================
    // GET CURRENT USER
    // =========================
    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated())
            return null;

        return userRepository.findByUsername(authentication.getName()).orElse(null);
    }
}