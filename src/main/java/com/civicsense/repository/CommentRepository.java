package com.civicsense.repository;

import com.civicsense.entity.Comment;
import com.civicsense.entity.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByIssue(Issue issue, Sort sort);
}
