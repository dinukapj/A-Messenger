package com.kaodim.messenger.models;

import java.util.Date;

/**
 * Created by Kanskiy on 19/10/2016.
 */

public class Message implements MessageModel{

    private String id;
    private boolean isReply;
    private Date createdAt;
    private Content content;

    public class Content implements MessageModel.Content{
        String text;
        Attachment attachment;
        @Override
        public String getText() {
            return text;
        }

        @Override
        public Attachment getAttachment() {
            return attachment;
        }
        public class Attachment implements MessageModel.Content.Attachment{
            private String original;
            private String thumb;
            private String fileName;

            @Override
            public String getOriginal() {
                return original;
            }

            @Override
            public String getThumb() {
                return thumb;
            }

            @Override
            public String getFileName() {
                return fileName;
            }
        }
    }


    @Override
    public String getId() {
        return id;
    }

    @Override
    public Boolean getIsOutgoingMessage() {
        return isReply;
    }

    @Override
    public Date getDate() {
        return createdAt;
    }

    @Override
    public MessageModel.Content getContent() {
        return content;
    }


}
