package com.kaodim.messenger.models;

/**
 * Created by Kanskiy on 24/10/2016.
 */


public class PushNotificationModel {
    public String conversationId;
    public String sender;
    public String message;
    public PushNotificationModel(String conversationId, String sender, String message){
        this.conversationId = conversationId;
        this.sender=sender;
        this.message=message;
    }
}
