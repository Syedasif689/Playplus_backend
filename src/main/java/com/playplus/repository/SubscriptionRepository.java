package com.playplus.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.playplus.model.Subscription;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Query("SELECT s FROM Subscription s WHERE s.channel.id = :channelId AND s.isActive = true")
    List<Subscription> findActiveSubscriptionsByChannelId(@Param("channelId") Long channelId);

    @Query("SELECT s FROM Subscription s WHERE s.subscriber.id = :subscriberId AND s.isActive = true")
    List<Subscription> findActiveSubscriptionsBySubscriberId(@Param("subscriberId") Long subscriberId);

    @Query("SELECT COUNT(s) > 0 FROM Subscription s WHERE s.subscriber.id = :subscriberId AND s.channel.id = :channelId AND s.isActive = true")
    boolean existsActiveSubscription(@Param("subscriberId") Long subscriberId, @Param("channelId") Long channelId);

    @Query("SELECT s FROM Subscription s WHERE s.subscriber.id = :subscriberId AND s.channel.id = :channelId AND s.isActive = true")
    Optional<Subscription> findActiveSubscription(@Param("subscriberId") Long subscriberId, @Param("channelId") Long channelId);

    // ✅ NEW: Find ANY subscription (active or inactive) by subscriber and channel
    @Query("SELECT s FROM Subscription s WHERE s.subscriber.id = :subscriberId AND s.channel.id = :channelId")
    Optional<Subscription> findBySubscriberIdAndChannelId(@Param("subscriberId") Long subscriberId, @Param("channelId") Long channelId);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.channel.id = :channelId AND s.isActive = true")
    Long countActiveSubscribersByChannelId(@Param("channelId") Long channelId);

    @Modifying
    @Transactional
    @Query("UPDATE Subscription s SET s.isActive = false WHERE s.subscriber.id = :userId OR s.channel.id = :userId")
    void deactivateAllUserSubscriptions(@Param("userId") Long userId);

    List<Subscription> findBySubscriberIdOrChannelId(Long subscriberId, Long channelId);

    boolean existsBySubscriberIdAndIsActiveTrue(Long subscriberId);
}