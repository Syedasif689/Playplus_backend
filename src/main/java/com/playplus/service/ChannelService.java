package com.playplus.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.playplus.dto.ChannelResponse;
import com.playplus.dto.SubscriptionResponse;
import com.playplus.model.Subscription;
import com.playplus.model.User;
import com.playplus.model.Video;
import com.playplus.repository.SubscriptionRepository;
import com.playplus.repository.UserRepository;
import com.playplus.repository.VideoRepository;

@Service
public class ChannelService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private VideoRepository videoRepository;

    /**
     * Get channel information for a specific username
     * 
     * @param username - The channel owner's username
     * @param currentUserId - The ID of the currently authenticated user (can be null for guests)
     * @return ChannelResponse with all channel data
     */
    public ChannelResponse getChannelInfo(String username, Long currentUserId) {
        // Find the channel owner
        System.out.println("Requested username = " + username);
        userRepository.findAll().forEach(u ->
       System.out.println("DB USER = " + u.getUsername())
      );
        User channelOwner = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Channel not found: " + username));

        // Get subscriber count
        Long subscriberCount = subscriptionRepository.countActiveSubscribersByChannelId(channelOwner.getId());
        
        // Check if current user is subscribed
        boolean isSubscribed = false;
        if (currentUserId != null) {
            isSubscribed = subscriptionRepository.existsActiveSubscription(currentUserId, channelOwner.getId());
        }
        
        // Check if this is the user's own channel
        boolean isOwnChannel = currentUserId != null && currentUserId.equals(channelOwner.getId());
        
        // Get video count
        List<Video> channelVideos = videoRepository.findByCreatorId(channelOwner.getId());
        int videoCount = channelVideos.size();

        // Build response using builder pattern
        return ChannelResponse.builder()
                .id(channelOwner.getId())
                .username(channelOwner.getUsername())
                .fullName(channelOwner.getFullName())
                .bio(channelOwner.getBio())
                .profileImage(channelOwner.getProfileImage())
                .subscriberCount(subscriberCount.intValue())
                .isSubscribed(isSubscribed)
                .isOwnChannel(isOwnChannel)
                .videoCount(videoCount)
                .build();
    }

    /**
     * Subscribe to a channel – reactivates if previously unsubscribed.
     * 
     * @param subscriberId - The ID of the user subscribing
     * @param channelUsername - The username of the channel to subscribe to
     * @return SubscriptionResponse with updated status and count
     */
    @Transactional
    public SubscriptionResponse subscribeToChannel(Long subscriberId, String channelUsername) {
        // Find subscriber
        User subscriber = userRepository.findById(subscriberId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find channel owner
        User channelOwner = userRepository.findByUsername(channelUsername)
                .orElseThrow(() -> new RuntimeException("Channel not found: " + channelUsername));

        // Prevent self-subscription
        if (subscriberId.equals(channelOwner.getId())) {
            throw new RuntimeException("You cannot subscribe to your own channel");
        }

        // Check if there is an existing subscription (active or inactive)
        Optional<Subscription> existingSubscription = subscriptionRepository
                .findBySubscriberIdAndChannelId(subscriberId, channelOwner.getId());

        Subscription subscription;
        if (existingSubscription.isPresent()) {
            subscription = existingSubscription.get();
            // If already active, throw error
            if (subscription.getIsActive()) {
                throw new RuntimeException("Already subscribed to this channel");
            }
            // Reactivate the existing subscription
            subscription.setIsActive(true);
            subscription.setSubscribedAt(LocalDateTime.now());
        } else {
            // Create a new subscription
            subscription = new Subscription();
            subscription.setSubscriber(subscriber);
            subscription.setChannel(channelOwner);
            subscription.setSubscribedAt(LocalDateTime.now());
            subscription.setIsActive(true);
        }

        // Save the subscription
        subscriptionRepository.save(subscription);

        // Update subscriber count on User entity (denormalized for performance)
        Long newCount = subscriptionRepository.countActiveSubscribersByChannelId(channelOwner.getId());
        channelOwner.setSubscriberCount(newCount.intValue());
        userRepository.save(channelOwner);

        // Return response
        return new SubscriptionResponse(
                true,
                newCount.intValue(),
                "Successfully subscribed to " + channelUsername
        );
    }

    /**
     * Unsubscribe from a channel
     * 
     * @param subscriberId - The ID of the user unsubscribing
     * @param channelUsername - The username of the channel to unsubscribe from
     * @return SubscriptionResponse with updated status and count
     */
    @Transactional
    public SubscriptionResponse unsubscribeFromChannel(Long subscriberId, String channelUsername) {
        // Find channel owner
        User channelOwner = userRepository.findByUsername(channelUsername)
                .orElseThrow(() -> new RuntimeException("Channel not found: " + channelUsername));

        // Find active subscription
        Subscription subscription = subscriptionRepository
                .findActiveSubscription(subscriberId, channelOwner.getId())
                .orElseThrow(() -> new RuntimeException("You are not subscribed to this channel"));

        // Soft delete: deactivate subscription
        subscription.setIsActive(false);
        subscriptionRepository.save(subscription);

        // Update subscriber count on User entity
        Long newCount = subscriptionRepository.countActiveSubscribersByChannelId(channelOwner.getId());
        channelOwner.setSubscriberCount(newCount.intValue());
        userRepository.save(channelOwner);

        // Return response
        return new SubscriptionResponse(
                false,
                newCount.intValue(),
                "Successfully unsubscribed from " + channelUsername
        );
    }

    /**
     * Get all channels a user is subscribed to
     * 
     * @param subscriberId - The ID of the user
     * @return List of channel usernames (or full channel objects)
     */
    public List<Subscription> getUserSubscriptions(Long subscriberId) {
        return subscriptionRepository.findActiveSubscriptionsBySubscriberId(subscriberId);
    }

    /**
     * Check if a user is subscribed to a channel
     * 
     * @param userId - The user's ID
     * @param channelId - The channel's ID
     * @return boolean indicating subscription status
     */
    public boolean isUserSubscribed(Long userId, Long channelId) {
        return subscriptionRepository.existsActiveSubscription(userId, channelId);
    }

    /**
     * Get subscriber count for a channel
     * 
     * @param channelId - The channel's ID
     * @return Long subscriber count
     */
    public Long getSubscriberCount(Long channelId) {
        return subscriptionRepository.countActiveSubscribersByChannelId(channelId);
    }

    /**
     * Get channel owner by username (helper method)
     * 
     * @param username - The username to look up
     * @return User object
     */
    public User getChannelOwner(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Channel not found: " + username));
    }
}