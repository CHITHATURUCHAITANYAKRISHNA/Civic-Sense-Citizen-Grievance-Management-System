package com.civicsense.service;

import com.civicsense.entity.Issue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationHelper {

    @Autowired
    private EmailService emailService;

    @Autowired
    private SMSService smsService;

    /**
     * Call this method whenever a complaint status changes
     * Sends both Email and SMS notification to the user
     */
    public void notifyUserStatusChange(Issue issue) {
        try {
            // 1️⃣ Send Email notification
            emailService.sendComplaintStatusEmail(
                    issue.getUser().getEmail(),
                    issue.getUser().getFullName(),  // use fullName instead of getName()
                    issue.getId(),
                    issue.getStatus().name()         // convert Status enum to String
            );

            // 2️⃣ Send SMS notification via Fast2SMS
            if (issue.getUser().getPhone() != null && !issue.getUser().getPhone().isBlank()) {
                String smsText = "Your CivicSense complaint #" + issue.getId() +
                                 " status is now " + issue.getStatus().name();
                smsService.sendSms(issue.getUser().getPhone(), smsText);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}