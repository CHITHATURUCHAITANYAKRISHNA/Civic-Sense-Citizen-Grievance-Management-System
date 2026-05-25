package com.civicsense.repository;

import com.civicsense.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // =========================
    // LOGIN SUPPORT
    // =========================
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    // =========================
    // NEW: Reset Password Support
    // =========================
    Optional<User> findByResetToken(String resetToken);

    // =========================
    // EXISTENCE CHECKS
    // =========================
    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    // =========================
    // 🔥 IMPORTANT: FIND ADMINS
    // =========================
    List<User> findByRoleContainingIgnoreCase(String role);
}