package com.kaodim.messenger.models;

import java.util.Date;

/**
 * Created by Kanskiy on 13/10/2016.
 */

public interface MessageModel {
    public String getId();
    public Boolean getIsOutgoingMessage();
    public Date getDate();
    public Content getContent();
    public interface Content {
        public String getText();
        public Attachment getAttachment();
        public interface Attachment{
            public String getOriginal();
            public String getThumb();
            public String getFileName();
        }
    }
}
