package com.playplus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.playplus.model.VideoHistory;

@Repository
public interface VideoHistoryRepository extends JpaRepository<VideoHistory, Long> {
    Optional<VideoHistory> findByUserIdAndVideoId(Long userId, Long videoId);
    List<VideoHistory> findByUserIdOrderByWatchedAtDesc(Long userId);
    void deleteByUserIdAndVideoId(Long userId, Long videoId);
    void deleteAllByUserId(Long userId);
}