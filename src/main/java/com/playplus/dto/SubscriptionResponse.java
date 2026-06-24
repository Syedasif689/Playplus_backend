package com.playplus.dto;

public class SubscriptionResponse {
    private Boolean subscribed;
    private Integer subscriberCount;
    private String message;
    
    // Default constructor
    public SubscriptionResponse() {}
    
    // Constructor
    public SubscriptionResponse(Boolean subscribed, Integer subscriberCount, String message) {
        this.subscribed = subscribed;
        this.subscriberCount = subscriberCount;
        this.message = message;
    }
    
    // Getters and Setters
    public Boolean getSubscribed() {
        return subscribed;
    }
    
    public void setSubscribed(Boolean subscribed) {
        this.subscribed = subscribed;
    }
    
    public Integer getSubscriberCount() {
        return subscriberCount;
    }
    
    public void setSubscriberCount(Integer subscriberCount) {
        this.subscriberCount = subscriberCount;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}