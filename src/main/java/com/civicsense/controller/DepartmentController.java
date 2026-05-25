package com.civicsense.controller;

import com.civicsense.entity.Department;
import com.civicsense.entity.Issue;
import com.civicsense.entity.Status;
import com.civicsense.entity.User;
import com.civicsense.repository.IssueRepository;
import com.civicsense.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/department")
public class DepartmentController {

    private final IssueRepository issueRepository;
    private final UserRepository userRepository;

    public DepartmentController(IssueRepository issueRepository,
                                UserRepository userRepository) {
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
    }

    // ===============================
    // 1️⃣ Department Dashboard
    // ===============================
    @GetMapping("/dashboard")
    public String departmentDashboard(Authentication authentication, Model model) {

        String username = authentication.getName();

        Optional<User> optionalUser =
                userRepository.findByUsername(username);

        if (optionalUser.isEmpty()) {
            model.addAttribute("issues", List.of());
            return "department/department-dashboard";
        }

        User departmentUser = optionalUser.get();
        Department department = departmentUser.getDepartment();

        if (department == null) {
            model.addAttribute("issues", List.of());
            return "department/department-dashboard";
        }

        // Fetch issues assigned to this department
        List<Issue> departmentIssues =
                issueRepository.findByDepartment_Id(department.getId());

        model.addAttribute("issues", departmentIssues);
        model.addAttribute("departmentName", department.getName());

        return "department/department-dashboard";
    }

    // ===============================
    // 2️⃣ Update Issue Status
    // ===============================
    @PostMapping("/update-status/{id}")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam String status) {

        Optional<Issue> optionalIssue =
                issueRepository.findById(id);

        if (optionalIssue.isPresent()) {

            Issue issue = optionalIssue.get();

            // Convert String to Enum safely
            Status newStatus = Status.valueOf(status);

            issue.setStatus(newStatus);
            issueRepository.save(issue);
        }

        return "redirect:/department/dashboard";
    }

    // ===============================
    // 3️⃣ View Issue Map (Department)
    // ===============================
    @GetMapping("/fullMap")
    public String viewFullMap(@RequestParam Long issueId, Model model) {

        Optional<Issue> optionalIssue =
                issueRepository.findById(issueId);

        if (optionalIssue.isPresent()) {

            Issue issue = optionalIssue.get();

            model.addAttribute("lat", issue.getLatitude());
            model.addAttribute("lng", issue.getLongitude());
        }

        return "department/department-full-map";
    }
}
