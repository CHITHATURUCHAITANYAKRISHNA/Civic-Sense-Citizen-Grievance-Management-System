package com.civicsense.controller;

import com.civicsense.entity.User;
import com.civicsense.service.CustomUserDetailsService;
import com.civicsense.service.EmailService;
import com.civicsense.service.SMSService;
import com.civicsense.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private CustomUserDetailsService userService;

    @Autowired
    private UserService customUserService;  // For forgot/reset password

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SMSService smsService; // SMS service

    @Autowired
    private EmailService emailService; // Email service

    // ------------------------------
    // Registration page
    // ------------------------------
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register"; // Thymeleaf template: register.html
    }

    // ------------------------------
    // Handle registration
    // ------------------------------
    @PostMapping("/register")
    public String register(@ModelAttribute("user") User user,
                           BindingResult result,
                           Model model) {

        // Validate form
        if (result.hasErrors()) {
            return "register";
        }

        // Check if email already exists
        if (userService.existsByEmail(user.getEmail())) {
            model.addAttribute("registrationError", "Email already registered!");
            return "register";
        }

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Default role for normal users
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("ROLE_USER");
        }

        // Save user
        userService.saveUser(user);

        // ================================
        // Send SMS confirmation
        // ================================
        String smsStatus = null;
        try {
            if (user.getPhone() != null && !user.getPhone().isBlank()) {
                // Remove non-digit characters
                String formattedNumber = user.getPhone().replaceAll("[^0-9]", "");

                // Validate 10-digit number
                if (!formattedNumber.matches("\\d{10}")) {
                    smsStatus = "⚠ Invalid mobile number: must be exactly 10 digits.";
                } else {
                    // Add country code
                    formattedNumber = "91" + formattedNumber;

                    // Send SMS
                    boolean sent = smsService.sendSms(formattedNumber,
                            "Hello " + user.getUsername() + ", your CivicSense account has been created successfully!");

                    // Print API response for debugging
                    String lastApiResponse = smsService.getLastApiResponse(); // Implement in SMSService
                    System.out.println("Fast2SMS Response: " + lastApiResponse);

                    smsStatus = sent ? "✅ SMS sent successfully to " + formattedNumber
                                     : "⚠ SMS could not be sent to " + formattedNumber;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            smsStatus = "⚠ SMS sending failed due to an error.";
        }

        // ================================
        // Send Email confirmation
        // ================================
        String emailStatus = null;
        try {
            if (user.getEmail() != null && !user.getEmail().isBlank()) {
                String subject = "CivicSense | Registration Successful";
                String body = "Hello " + user.getUsername() + ",\n\n" +
                        "Your CivicSense account has been created successfully!\n\n" +
                        "You can now login using your email and password.\n\n" +
                        "Regards,\nCivicSense Team";
                emailService.sendEmailWithCcBcc(user.getEmail(), null, null, subject, body);
                emailStatus = "✅ Email sent successfully to " + user.getEmail();
            }
        } catch (Exception e) {
            e.printStackTrace(); // ✅ Full stack trace for debugging
            emailStatus = "⚠ Email sending failed due to an error.";
        }

        model.addAttribute("registrationSuccess", "Registration successful! Please login.");
        model.addAttribute("smsStatus", smsStatus);
        model.addAttribute("emailStatus", emailStatus);

        return "register"; // show register page with success and status
    }

    // ------------------------------
    // Login page
    // ------------------------------
    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // Thymeleaf template: login.html
    }

    // =========================
    // Forgot Password
    // =========================
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam("email") String email,
                                       Model model) {

        boolean emailSent = customUserService.generateResetToken(email);
        if (emailSent) {
            model.addAttribute("successMessage", "If this email exists, a reset link has been sent.");
        } else {
            model.addAttribute("errorMessage", "If this email exists, a reset link has been sent.");
        }
        return "forgot-password";
    }

    // =========================
    // Reset Password
    // =========================
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token,
                                        Model model) {

        if (customUserService.validateResetToken(token).isEmpty()) {
            model.addAttribute("errorMessage", "Invalid or expired token.");
            return "login";
        }
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(@RequestParam("token") String token,
                                      @RequestParam("password") String password,
                                      Model model) {

        boolean updated = customUserService.updatePassword(token, password);
        if (updated) {
            model.addAttribute("successMessage", "Password successfully reset. You can now login.");
            return "login";
        } else {
            model.addAttribute("errorMessage", "Invalid or expired token.");
            return "reset-password";
        }
    }
}