package com.playplus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.playplus.model.Video;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findByCreator(String creator);
    List<Video> findByCreatorId(Long creatorId);
}