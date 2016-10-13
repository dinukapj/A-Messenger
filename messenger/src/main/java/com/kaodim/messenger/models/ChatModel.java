package com.kaodim.messenger.models;

import java.util.ArrayList;

/**
 * Created by Kanskiy on 12/10/2016.
 */

public interface ChatModel {
    public String getConversationId();
    public ArrayList<? extends MessageModel> getMessages();
}
