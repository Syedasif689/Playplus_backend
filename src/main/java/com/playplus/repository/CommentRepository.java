package com.playplus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.playplus.model.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByVideoIdAndParentCommentIdIsNull(Long videoId);
    List<Comment> findByVideoIdOrderByCreatedAtDesc(Long videoId);
    List<Comment> findByParentCommentId(Long parentCommentId);
    List<Comment> findByVideoIdAndUserId(Long videoId, Long userId);
}