package com.kaodim.messenger.models;

import java.util.Date;

/**
 * Created by Kanskiy on 19/10/2016.
 */

public class Message {
    public String id;
    public String sender_id;
    public String receiver_id;
    public String group_id;
    public String name;
    public Content content;
    public Boolean read;
    public Boolean spam;
    public Date created_at;
    public Date updated_at;
    public Attachment attachment;

    public class Content{
        public String text;
        public String attachment_file_id;

        public class Location{
            public float latitude;
            public float longitude;
        }
    }

}
