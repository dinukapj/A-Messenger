package com.kaodim.messenger.models;

/**
 * Created by Kanskiy on 24/10/2016.
 */


public class MessagePushModel {
    public String conversationId;
    public String sender;
    public String message;
    public MessagePushModel(String conversationId, String sender, String message){
        this.conversationId = conversationId;
        this.sender=sender;
        this.message=message;
    }
}
