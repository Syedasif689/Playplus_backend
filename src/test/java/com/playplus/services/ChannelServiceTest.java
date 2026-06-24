package com.playplus.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.playplus.dto.SubscriptionResponse;
import com.playplus.model.Subscription;
import com.playplus.model.User;
import com.playplus.repository.SubscriptionRepository;
import com.playplus.repository.UserRepository;
import com.playplus.repository.VideoRepository;

@ExtendWith(MockitoExtension.class)
class ChannelServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private VideoRepository videoRepository;

    @InjectMocks
    private ChannelService channelService;

    private User subscriber;
    private User channelOwner;

    @BeforeEach
    void setUp() {
        subscriber = new User();
        subscriber.setId(1L);
        subscriber.setUsername("testUser");

        channelOwner = new User();
        channelOwner.setId(2L);
        channelOwner.setUsername("channelOwner");
        channelOwner.setSubscriberCount(0);
    }

    // Helper to create an active subscription
    private Subscription createActiveSubscription() {
        Subscription sub = new Subscription();
        sub.setId(100L);
        sub.setSubscriber(subscriber);
        sub.setChannel(channelOwner);
        sub.setIsActive(true);
        return sub;
    }

    // Helper to create an inactive subscription
    private Subscription createInactiveSubscription() {
        Subscription sub = new Subscription();
        sub.setId(101L);
        sub.setSubscriber(subscriber);
        sub.setChannel(channelOwner);
        sub.setIsActive(false);
        return sub;
    }

    @Test
    void shouldSubscribeUserToChannel() {
        // Given: No existing subscription, so we create a new one.
        when(userRepository.findById(1L)).thenReturn(Optional.of(subscriber));
        when(userRepository.findByUsername("channelOwner")).thenReturn(Optional.of(channelOwner));
        when(subscriptionRepository.findBySubscriberIdAndChannelId(1L, 2L))
                .thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(subscriptionRepository.countActiveSubscribersByChannelId(2L)).thenReturn(1L);
        when(userRepository.save(any(User.class))).thenReturn(channelOwner);

        // When
        SubscriptionResponse response = channelService.subscribeToChannel(1L, "channelOwner");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getSubscribed()).isTrue();
        assertThat(response.getSubscriberCount()).isEqualTo(1);
        assertThat(response.getMessage()).isEqualTo("Successfully subscribed to channelOwner");
        verify(subscriptionRepository, times(1)).save(any(Subscription.class));
        verify(userRepository, times(1)).save(channelOwner);
        assertThat(channelOwner.getSubscriberCount()).isEqualTo(1);
    }

    @Test
    void shouldUnsubscribeFromChannel() {
        // Given: An active subscription exists.
        when(userRepository.findByUsername("channelOwner")).thenReturn(Optional.of(channelOwner));
        when(subscriptionRepository.findActiveSubscription(1L, 2L))
                .thenReturn(Optional.of(createActiveSubscription()));
        when(subscriptionRepository.countActiveSubscribersByChannelId(2L)).thenReturn(0L);
        when(userRepository.save(any(User.class))).thenReturn(channelOwner);

        // When
        SubscriptionResponse response = channelService.unsubscribeFromChannel(1L, "channelOwner");

        // Then
        assertThat(response.getSubscribed()).isFalse();
        assertThat(response.getSubscriberCount()).isZero();
        verify(subscriptionRepository, times(1)).save(any(Subscription.class));
        verify(userRepository, times(1)).save(channelOwner);
    }

    @Test
    void shouldNotSubscribeWhenAlreadyActive() {
        // Given: An active subscription already exists.
        when(userRepository.findById(1L)).thenReturn(Optional.of(subscriber));
        when(userRepository.findByUsername("channelOwner")).thenReturn(Optional.of(channelOwner));
        when(subscriptionRepository.findBySubscriberIdAndChannelId(1L, 2L))
                .thenReturn(Optional.of(createActiveSubscription()));

        // When / Then: The service should throw.
        assertThatThrownBy(() -> channelService.subscribeToChannel(1L, "channelOwner"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Already subscribed to this channel");

        // Verify that save was never called.
        verify(subscriptionRepository, never()).save(any(Subscription.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldNotAllowSelfSubscription() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(subscriber));
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(subscriber));

        assertThatThrownBy(() -> channelService.subscribeToChannel(1L, "testUser"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("You cannot subscribe to your own channel");
    }

    @Test
    void shouldReactivateInactiveSubscription() {
        // Given: An inactive subscription exists.
        when(userRepository.findById(1L)).thenReturn(Optional.of(subscriber));
        when(userRepository.findByUsername("channelOwner")).thenReturn(Optional.of(channelOwner));
        when(subscriptionRepository.findBySubscriberIdAndChannelId(1L, 2L))
                .thenReturn(Optional.of(createInactiveSubscription()));
        when(subscriptionRepository.save(any(Subscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(subscriptionRepository.countActiveSubscribersByChannelId(2L)).thenReturn(1L);
        when(userRepository.save(any(User.class))).thenReturn(channelOwner);

        // When
        SubscriptionResponse response = channelService.subscribeToChannel(1L, "channelOwner");

        // Then
        assertThat(response.getSubscribed()).isTrue();
        assertThat(response.getSubscriberCount()).isEqualTo(1);
        verify(subscriptionRepository, times(1)).save(any(Subscription.class));
        verify(userRepository, times(1)).save(channelOwner);
    }
}