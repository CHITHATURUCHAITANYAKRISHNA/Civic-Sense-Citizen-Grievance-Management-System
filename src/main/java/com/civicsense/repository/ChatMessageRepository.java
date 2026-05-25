package com.civicsense.repository;

import com.civicsense.entity.ChatMessage;
import com.civicsense.entity.Issue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // Get all messages for one issue ordered by time
    List<ChatMessage> findByIssueOrderByTimestampAsc(Issue issue);

}