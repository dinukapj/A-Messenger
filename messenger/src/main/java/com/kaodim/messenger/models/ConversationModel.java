package com.kaodim.messenger.models;

import java.util.Date;

/**
 * Created by Kanskiy on 11/10/2016.
 */

public interface ConversationModel {
    public String getId();
    public String getAvatar();
    public String getName();
    public String getLastMessage();
    public Date getDate();
    public int getUnreadMessagesCount();
}
