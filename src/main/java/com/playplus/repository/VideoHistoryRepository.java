package com.playplus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.playplus.model.VideoHistory;

@Repository
public interface VideoHistoryRepository extends JpaRepository<VideoHistory, Long> {

    Optional<VideoHistory> findByUserIdAndVideoId(Long userId, Long videoId);

    List<VideoHistory> findByUserIdOrderByWatchedAtDesc(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM VideoHistory vh WHERE vh.user.id = :userId AND vh.video.id = :videoId")
      void deleteByUserIdAndVideoId(
        @Param("userId") Long userId,
        @Param("videoId") Long videoId);

    @Modifying
    @Transactional
    @Query("DELETE FROM VideoHistory vh WHERE vh.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    void deleteAllByVideoId(Long videoId);
}