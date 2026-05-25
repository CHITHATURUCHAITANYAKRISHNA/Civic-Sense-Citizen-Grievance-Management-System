package com.civicsense.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Each message belongs to one Issue
    @ManyToOne
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    // Who sent the message (email)
    @Column(nullable = false)
    private String senderEmail;

    // Role of sender (ROLE_USER or ROLE_ADMIN)
    @Column(nullable = false)
    private String senderRole;

    // Actual message text
    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    // Timestamp
    @Column(nullable = false)
    private LocalDateTime timestamp;

    // Automatically set time before saving
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }

    // ===== Getters and Setters =====

    public Long getId() {
        return id;
    }

    public Issue getIssue() {
        return issue;
    }

    public void setIssue(Issue issue) {
        this.issue = issue;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(String senderRole) {
        this.senderRole = senderRole;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}