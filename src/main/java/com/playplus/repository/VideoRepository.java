package com.playplus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.playplus.model.Video;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    List<Video> findByCreator(String creator);

    List<Video> findByCreatorId(Long creatorId);

    @Modifying
    @Transactional
    @Query("""
        UPDATE Video v
        SET v.creator = :creator
        WHERE v.creatorId = :creatorId
    """)
    void updateCreatorName(
            @Param("creatorId") Long creatorId,
            @Param("creator") String creator
    );
}