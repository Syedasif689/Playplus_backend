package com.playplus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.playplus.model.SocialLink;
import com.playplus.model.User;

@Repository
public interface SocialLinkRepository extends JpaRepository<SocialLink, Long> {

    List<SocialLink> findByUser(User user);

    void deleteByUser(User user);
}