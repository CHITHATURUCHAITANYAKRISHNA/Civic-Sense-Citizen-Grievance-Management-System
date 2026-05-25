package com.civicsense.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activities")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // What the user did (e.g., "Submitted complaint: Pothole on Main St")
    @Column(nullable = false)
    private String description;

    // When the action happened
    @Column(nullable = false)
    private LocalDateTime timestamp;

    // Which user performed the activity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ================= Constructors =================
    public Activity() {}

    // Easy constructor for logging new activities
    public Activity(User user, String description) {
        this.user = user;
        this.description = description;
        this.timestamp = LocalDateTime.now(); // auto-set timestamp
    }

    public Activity(String description, LocalDateTime timestamp, User user) {
        this.description = description;
        this.timestamp = timestamp;
        this.user = user;
    }

    // ================= Getters & Setters =================
    public Long getId() { return id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
