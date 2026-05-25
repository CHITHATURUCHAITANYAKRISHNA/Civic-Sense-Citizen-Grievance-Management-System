package com.civicsense.controller;

import com.civicsense.entity.Department;
import com.civicsense.entity.Issue;
import com.civicsense.entity.Status;
import com.civicsense.entity.User;
import com.civicsense.entity.Notification;
import com.civicsense.repository.DepartmentRepository;
import com.civicsense.repository.IssueRepository;
import com.civicsense.repository.NotificationRepository;
import com.civicsense.repository.UserRepository;
import com.civicsense.service.EmailService;
import com.civicsense.service.SMSService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService; // ✅ EmailService integrated

    @Autowired
    private SMSService smsService; // ✅ SMSService integrated

    private final Map<String, String> resetTokens = new ConcurrentHashMap<>();

    public AdminController(IssueRepository issueRepository,
                           UserRepository userRepository,
                           DepartmentRepository departmentRepository,
                           NotificationRepository notificationRepository) {
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.notificationRepository = notificationRepository;
    }

    // =========================
    // ADMIN DASHBOARD
    // =========================
    @GetMapping("/dashboard")
    public String adminDashboard(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("isAdmin", true);

        Map<String, Long> stats = new HashMap<>();
        stats.put("Total Users", userRepository.count());
        stats.put("Pending Reports", issueRepository.countByStatus(Status.PENDING));
        stats.put("In Progress Reports", issueRepository.countByStatus(Status.IN_PROGRESS));
        stats.put("Resolved Reports", issueRepository.countByStatus(Status.RESOLVED));
        stats.put("System Alerts", 0L);
        stats.put("Active Sessions", 0L);
        model.addAttribute("stats", stats);

        List<Issue> recentIssues = issueRepository.findTop5ByOrderByCreatedAtDesc();
        model.addAttribute("recentIssues", recentIssues);

        User adminUser = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (adminUser != null) {
            long unreadCount = notificationRepository.countByUserAndReadFalse(adminUser);
            List<Notification> latestNotifications =
                    notificationRepository.findTop5ByUserOrderByTimestampDesc(adminUser);

            model.addAttribute("unreadCount", unreadCount);
            model.addAttribute("latestNotifications", latestNotifications);
        }

        return "admin-dashboard";
    }

    // =========================
    // GET NOTIFICATIONS (AJAX)
    // =========================
    @GetMapping("/notifications")
    @ResponseBody
    public List<Map<String, Object>> getNotifications(Authentication authentication) {
        User adminUser = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (adminUser == null) return List.of();

        List<Notification> notifications =
                notificationRepository.findTop5ByUserOrderByTimestampDesc(adminUser);

        return notifications.stream().map(n -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", n.getId());
            map.put("message", n.getMessage());
            map.put("read", n.isRead());
            return map;
        }).toList();
    }

    // =========================
    // MARK NOTIFICATION AS READ (AJAX)
    // =========================
    @PostMapping("/notifications/read/{id}")
    @ResponseBody
    public String markNotificationAsRead(@PathVariable Long id) {
        notificationRepository.findById(id).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
        return "success";
    }

    // =========================
    // USERS PAGE
    // =========================
    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userRepository.findById(id).ifPresent(userRepository::delete);
        return "redirect:/admin/users";
    }

    // =========================
    // CREATE ADMIN
    // =========================
    @GetMapping("/create")
    public String createAdminForm(Model model) {
        model.addAttribute("user", new User());
        return "admin/admin-create";
    }

    @PostMapping("/create")
    public String createAdminSubmit(@ModelAttribute("user") User user, Model model) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_ADMIN");
        userRepository.save(user);

        model.addAttribute("message", "Admin created successfully!");
        model.addAttribute("user", new User());
        return "admin/admin-create";
    }

    // =========================
    // CREATE DEPARTMENT OFFICER
    // =========================
    @GetMapping("/create-officer")
    public String createOfficerForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("departments", departmentRepository.findAll());
        return "admin/admin-create-officer";
    }

    @PostMapping("/create-officer")
    public String createOfficerSubmit(@ModelAttribute("user") User user,
                                      @RequestParam("departmentId") Long departmentId,
                                      Model model) {

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_DEPARTMENT");
        departmentRepository.findById(departmentId).ifPresent(user::setDepartment);
        userRepository.save(user);

        model.addAttribute("message", "Department Officer created successfully!");
        model.addAttribute("user", new User());
        model.addAttribute("departments", departmentRepository.findAll());

        return "admin/admin-create-officer";
    }

    // =========================
    // COMPLAINTS PAGE WITH EMAIL AND SMS NOTIFICATIONS
    // =========================
    @GetMapping("/complaints")
    public String viewComplaints(@RequestParam(value = "status", required = false) String status,
                                 Model model) {

        List<Issue> complaints;

        if (status != null && !status.isBlank()) {
            switch (status.toLowerCase()) {
                case "pending" -> complaints = issueRepository.findByStatus(Status.PENDING);
                case "inprogress" -> complaints = issueRepository.findByStatus(Status.IN_PROGRESS);
                case "resolved" -> complaints = issueRepository.findByStatus(Status.RESOLVED);
                default -> complaints = issueRepository.findAll();
            }
        } else {
            complaints = issueRepository.findAll();
        }

        model.addAttribute("complaints", complaints);
        model.addAttribute("statusFilter", status != null ? status : "all");
        model.addAttribute("departments", departmentRepository.findAll());

        return "admin/admin-complaints";
    }

    @GetMapping("/update-status/{id}")
    public String showUpdateStatusForm(@PathVariable Long id, Model model) {
        Issue complaint = issueRepository.findById(id).orElse(null);
        if (complaint == null) return "redirect:/admin/complaints";

        model.addAttribute("complaint", complaint);
        model.addAttribute("statuses", Status.values());

        return "admin/admin-update-status";
    }

    @PostMapping("/update-status/{id}")
    public String handleUpdateStatus(@PathVariable Long id,
                                     @RequestParam("status") Status newStatus) {

        issueRepository.findById(id).ifPresent(issue -> {
            Status oldStatus = issue.getStatus();
            issue.setStatus(newStatus);
            issueRepository.save(issue);

            User issueUser = issue.getUser();
            if (issueUser != null && oldStatus != newStatus) {
                Notification notification = new Notification();
                notification.setUser(issueUser);
                notification.setMessage("Your issue #" + issue.getId() + " (" + issue.getTitle() + ") status has been updated to " + newStatus);
                notification.setRead(false);
                notificationRepository.save(notification);

                // ✅ Send Email
                try {
                    emailService.sendComplaintStatusEmail(issueUser.getEmail(), issueUser.getUsername(), issue.getId(), newStatus.name());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // ✅ Send SMS (corrected)
                try {
                    if (issueUser.getPhone() != null && !issueUser.getPhone().isBlank()) {
                        String smsMessage = "Your issue #" + issue.getId() + " (" + issue.getTitle() + ") status is now " + newStatus;
                        smsService.sendSms(issueUser.getPhone(), smsMessage);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return "redirect:/admin/complaints";
    }

    @PostMapping("/assign-department/{id}")
    public String assignDepartment(@PathVariable Long id,
                                   @RequestParam("departmentId") Long departmentId) {

        issueRepository.findById(id).ifPresent(issue -> {
            departmentRepository.findById(departmentId).ifPresent(dept -> {
                issue.setDepartment(dept);
                issueRepository.save(issue);

                User issueUser = issue.getUser();
                if (issueUser != null) {
                    Notification notification = new Notification();
                    notification.setUser(issueUser);
                    notification.setMessage("Your issue #" + issue.getId() + " (" + issue.getTitle() + ") has been assigned to the department: " + dept.getName());
                    notification.setRead(false);
                    notificationRepository.save(notification);

                    // ✅ Send Email
                    try {
                        emailService.sendComplaintStatusEmail(issueUser.getEmail(), issueUser.getUsername(), issue.getId(), "Assigned to " + dept.getName());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // ✅ Send SMS (corrected)
                    try {
                        if (issueUser.getPhone() != null && !issueUser.getPhone().isBlank()) {
                            String smsMessage = "Your issue #" + issue.getId() + " (" + issue.getTitle() + ") assigned to department: " + dept.getName();
                            smsService.sendSms(issueUser.getPhone(), smsMessage);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        });

        return "redirect:/admin/complaints";
    }

    // =========================
    // DEPARTMENT CRUD
    // =========================
    @GetMapping("/departments")
    public String viewDepartments(Model model) {
        model.addAttribute("departments", departmentRepository.findAll());
        return "admin/admin-departments";
    }

    @GetMapping("/departments/new")
    public String newDepartmentForm(Model model) {
        model.addAttribute("department", new Department());
        return "admin/admin-department-new";
    }

    @PostMapping("/departments/new")
    public String createDepartment(@ModelAttribute("department") Department department) {
        departmentRepository.save(department);
        return "redirect:/admin/departments";
    }

    @GetMapping("/departments/edit/{id}")
    public String editDepartmentForm(@PathVariable Long id, Model model) {
        Department department = departmentRepository.findById(id).orElse(null);
        if (department == null) return "redirect:/admin/departments";

        model.addAttribute("department", department);
        return "admin/admin-department-edit";
    }

    @PostMapping("/departments/edit/{id}")
    public String updateDepartment(@PathVariable Long id,
                                   @ModelAttribute("department") Department updatedDepartment) {

        departmentRepository.findById(id).ifPresent(department -> {
            department.setName(updatedDepartment.getName());
            departmentRepository.save(department);
        });

        return "redirect:/admin/departments";
    }

    @GetMapping("/departments/delete/{id}")
    public String deleteDepartment(@PathVariable Long id) {
        departmentRepository.deleteById(id);
        return "redirect:/admin/departments";
    }

    // =========================
    // EXPORT REPORTS (PDF / EXCEL)
    // =========================
    @GetMapping("/export-reports")
    public void exportReports(@RequestParam(name = "format", defaultValue = "pdf") String format,
                              HttpServletResponse response) throws IOException {

        List<Issue> allIssues = issueRepository.findAll();

        if ("excel".equalsIgnoreCase(format)) {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=issues.xlsx");

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Issues");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Title");
            header.createCell(2).setCellValue("Category");
            header.createCell(3).setCellValue("Status");
            header.createCell(4).setCellValue("Department");
            header.createCell(5).setCellValue("Created At");

            int rowNum = 1;
            for (Issue issue : allIssues) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(issue.getId());
                row.createCell(1).setCellValue(issue.getTitle() != null ? issue.getTitle() : "N/A");
                row.createCell(2).setCellValue(issue.getCategory() != null ? issue.getCategory().toString() : "N/A");
                row.createCell(3).setCellValue(issue.getStatus() != null ? issue.getStatus().name() : "N/A");
                row.createCell(4).setCellValue(issue.getDepartment() != null ? issue.getDepartment().getName() : "N/A");
                row.createCell(5).setCellValue(issue.getCreatedAt() != null ? issue.getCreatedAt().toString() : "N/A");
            }

            workbook.write(response.getOutputStream());
            workbook.close();

        } else if ("pdf".equalsIgnoreCase(format)) {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=issues.pdf");

            PdfWriter writer = new PdfWriter(response.getOutputStream());
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("CivicSense Issue Report").setBold().setFontSize(16));
            float[] columnWidths = {30f, 100f, 70f, 70f, 80f, 80f};
            Table table = new Table(columnWidths);

            table.addHeaderCell("ID");
            table.addHeaderCell("Title");
            table.addHeaderCell("Category");
            table.addHeaderCell("Status");
            table.addHeaderCell("Department");
            table.addHeaderCell("Created At");

            for (Issue issue : allIssues) {
                table.addCell(String.valueOf(issue.getId()));
                table.addCell(issue.getTitle() != null ? issue.getTitle() : "N/A");
                table.addCell(issue.getCategory() != null ? issue.getCategory().toString() : "N/A");
                table.addCell(issue.getStatus() != null ? issue.getStatus().name() : "N/A");
                table.addCell(issue.getDepartment() != null ? issue.getDepartment().getName() : "N/A");
                table.addCell(issue.getCreatedAt() != null ? issue.getCreatedAt().toString() : "N/A");
            }

            document.add(table);
            document.close();
        }
    }

    // =========================
    // FULL MAP VIEW FOR A SINGLE COMPLAINT
    // =========================
    @GetMapping("/full-map/{id}")
    public String viewFullMap(@PathVariable Long id, Model model) {

        Issue complaint = issueRepository.findById(id).orElse(null);

        if (complaint == null || complaint.getLatitude() == null || complaint.getLongitude() == null) {
            return "redirect:/admin/complaints";
        }

        model.addAttribute("complaint", complaint);
        model.addAttribute("lat", complaint.getLatitude());
        model.addAttribute("lng", complaint.getLongitude());

        return "admin/full-map";
    }

    // =========================
    // PASSWORD RESET FUNCTIONALITY
    // =========================
    @GetMapping("/password-reset-request")
    public String passwordResetRequestForm() {
        return "admin/password-reset-request";
    }

    @PostMapping("/password-reset-request")
    public String handlePasswordResetRequest(@RequestParam("email") String email, Model model) {

        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            resetTokens.put(token, user.getUsername());
            try {
                emailService.sendResetEmail(email, token);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        model.addAttribute("message", "If the email exists, a password reset link has been sent.");
        return "admin/password-reset-request";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam("token") String token, Model model) {
        String username = resetTokens.get(token);
        if (username == null) {
            model.addAttribute("error", "Invalid or expired token.");
            return "admin/reset-password";
        }
        model.addAttribute("token", token);
        return "admin/reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(@RequestParam("token") String token,
                                      @RequestParam("password") String newPassword,
                                      Model model) {

        String username = resetTokens.get(token);
        if (username == null) {
            model.addAttribute("error", "Invalid or expired token.");
            return "admin/reset-password";
        }

        userRepository.findByUsername(username).ifPresent(user -> {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        });

        resetTokens.remove(token);
        model.addAttribute("message", "Password updated successfully. You can now login.");
        return "admin/reset-password";
    }

}