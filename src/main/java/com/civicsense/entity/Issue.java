package com.civicsense.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "issues")
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Description is required")
    @Column(nullable = false, length = 1000)
    private String description;

    @NotNull(message = "Category is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // =========================
    // OLD SINGLE IMAGE (KEEPED)
    // =========================
    @Column(name = "image_path")
    private String imagePath;

    // =========================
    // NEW MULTIPLE IMAGES (comma separated)
    // =========================
    @Column(name = "image_paths", length = 2000)
    private String imagePaths;

    // =========================
    // NEW VIDEO PATH
    // =========================
    @Column(name = "video_path")
    private String videoPath;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // =========================
    // ASSIGNED DEPARTMENT
    // =========================
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;

    // =========================
    // LOCATION FIELDS
    // =========================
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    // =========================
    // Getters & Setters
    // =========================

    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    // NEW MULTIPLE IMAGES
    public String getImagePaths() { return imagePaths; }
    public void setImagePaths(String imagePaths) { this.imagePaths = imagePaths; }

    // NEW VIDEO
    public String getVideoPath() { return videoPath; }
    public void setVideoPath(String videoPath) { this.videoPath = videoPath; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    // =========================
    // JPA Lifecycle Hooks
    // =========================
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = Status.PENDING;
        }
    }

    // =========================
    // Constructors
    // =========================
    public Issue() {}

    public Issue(String title, String description, Category category, User user) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.user = user;
        this.status = Status.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // =========================
    // Debugging
    // =========================
    @Override
    public String toString() {
        return "Issue{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", category=" + category +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", imagePath='" + imagePath + '\'' +
                ", imagePaths='" + imagePaths + '\'' +
                ", videoPath='" + videoPath + '\'' +
                ", user=" + (user != null ? user.getUsername() : null) +
                ", department=" + (department != null ? department.getName() : null) +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}