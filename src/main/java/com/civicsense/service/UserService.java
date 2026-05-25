package com.civicsense.service;

import com.civicsense.entity.User;
import com.civicsense.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;   // ✅ Added

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService) {   // ✅ Added
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;     // ✅ Added
    }

    // =========================
    // 1️⃣ Generate Reset Token
    // =========================
    public boolean generateResetToken(String email) {

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return false;
        }

        User user = optionalUser.get();

        String token = UUID.randomUUID().toString();

        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(30));

        userRepository.save(user);

        // ✅ Send Reset Email (NEW LINE)
        emailService.sendResetEmail(user.getEmail(), token);

        return true;
    }

    // =========================
    // 2️⃣ Validate Token
    // =========================
    public Optional<User> validateResetToken(String token) {

        Optional<User> optionalUser = userRepository.findByResetToken(token);

        if (optionalUser.isEmpty()) {
            return Optional.empty();
        }

        User user = optionalUser.get();

        if (user.getResetTokenExpiry() == null ||
            user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {

            return Optional.empty();
        }

        return Optional.of(user);
    }

    // =========================
    // 3️⃣ Update Password
    // =========================
    public boolean updatePassword(String token, String newPassword) {

        Optional<User> optionalUser = validateResetToken(token);

        if (optionalUser.isEmpty()) {
            return false;
        }

        User user = optionalUser.get();

        user.setPassword(passwordEncoder.encode(newPassword));

        // Clear token after successful reset
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepository.save(user);

        return true;
    }
}