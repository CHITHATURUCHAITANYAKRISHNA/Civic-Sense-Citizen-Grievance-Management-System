package com.civicsense.controller;

import com.civicsense.entity.ChatMessage;
import com.civicsense.entity.Issue;
import com.civicsense.repository.ChatMessageRepository;
import com.civicsense.repository.IssueRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/chat")
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final IssueRepository issueRepository;

    public ChatController(ChatMessageRepository chatMessageRepository,
                          IssueRepository issueRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.issueRepository = issueRepository;
    }

    // ==========================
    // View chat for an issue
    // ==========================
    @GetMapping("/{issueId}")
    public String viewChat(@PathVariable Long issueId, Model model) {

        Issue issue = issueRepository.findById(issueId).orElseThrow();

        List<ChatMessage> messages =
                chatMessageRepository.findByIssueOrderByTimestampAsc(issue);

        model.addAttribute("issue", issue);
        model.addAttribute("messages", messages);

        return "chat";
    }

    // ==========================
    // Send message
    // ==========================
    @PostMapping("/send")
    public String sendMessage(@RequestParam Long issueId,
                              @RequestParam String message,
                              Authentication authentication) {

        Issue issue = issueRepository.findById(issueId).orElseThrow();

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setIssue(issue);
        chatMessage.setMessage(message);
        chatMessage.setSenderEmail(authentication.getName());

        // Determine role (USER or ADMIN)
        String role = authentication.getAuthorities()
                .iterator().next().getAuthority();
        chatMessage.setSenderRole(role);

        chatMessageRepository.save(chatMessage);

        return "redirect:/chat/" + issueId;
    }
}