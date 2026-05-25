package com.civicsense.repository;

import com.civicsense.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueRepository extends JpaRepository<Issue, Long> {

    // =========================
    // USER QUERIES (PAGINATION)
    // =========================
    Page<Issue> findByUser(User user, Pageable pageable);

    Page<Issue> findByUserAndCategory(User user, Category category, Pageable pageable);

    Page<Issue> findByUserAndStatus(User user, Status status, Pageable pageable);

    Page<Issue> findByUserAndCategoryAndStatus(User user,
                                               Category category,
                                               Status status,
                                               Pageable pageable);

    // =========================
    // USER SORT (FOR DASHBOARD)
    // =========================
    List<Issue> findByUser(User user, Sort sort);

    // =========================
    // ADMIN FILTERS (PAGINATION)
    // =========================
    Page<Issue> findByCategory(Category category, Pageable pageable);

    Page<Issue> findByStatus(Status status, Pageable pageable);

    Page<Issue> findByCategoryAndStatus(Category category,
                                        Status status,
                                        Pageable pageable);

    // =========================
    // ADMIN SIMPLE LIST FILTERS
    // =========================
    List<Issue> findByStatus(Status status);

    long countByStatus(Status status);

    // =========================
    // DEPARTMENT QUERY
    // =========================
    List<Issue> findByDepartment_Id(Long departmentId);

    // ✅ SAFE ADDITION: Department sorted by creation date
    List<Issue> findByDepartment_IdOrderByCreatedAtDesc(Long departmentId);

    // =========================
    // DASHBOARD - RECENT ISSUES
    // =========================
    List<Issue> findTop5ByOrderByCreatedAtDesc();

    // =========================
    // NEW: FILTER WITH OPTIONAL CATEGORY & STATUS FOR USERS & ADMIN
    // =========================
    default Page<Issue> findFiltered(User user, Category category, Status status, Pageable pageable) {
        if (user != null) { // Regular user
            if (category != null && status != null)
                return findByUserAndCategoryAndStatus(user, category, status, pageable);
            else if (category != null)
                return findByUserAndCategory(user, category, pageable);
            else if (status != null)
                return findByUserAndStatus(user, status, pageable);
            else
                return findByUser(user, pageable);
        } else { // Admin
            if (category != null && status != null)
                return findByCategoryAndStatus(category, status, pageable);
            else if (category != null)
                return findByCategory(category, pageable);
            else if (status != null)
                return findByStatus(status, pageable);
            else
                return findAll(pageable);
        }
    }
}