package com.kaodim.messenger.models;

import java.util.ArrayList;

/**
 * Created by Kanskiy on 19/10/2016.
 */

public class Chat implements ChatModel {
    public String serviceQuotationId;
    public String serviceRequestId;
    public String serviceRequestName;
    public String vendorBusinessName;
    public String vendorName;
    public String senderAvatar;
    public ArrayList<Message> posts;

    @Override
    public String getConversationId() {
        return serviceQuotationId;
    }

    @Override
    public ArrayList<? extends MessageModel> getMessages() {
        return posts;
    }
}
