package com.kaodim.messenger.tools;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.kaodim.messenger.models.ChatModel;
import com.kaodim.messenger.models.ConversationModel;
import com.kaodim.messenger.models.MessageModel;

import java.util.ArrayList;

/**
 * Created by Kanskiy on 19/10/2016.
 */

public abstract  class AMessenger {

    private final int MESSAGES_THRESHOLD = 10;


    private String conversationUrl;
    private String chatUrl;

    private static AMessenger defaultConfig;

    private AMessenger(String conversationUrl, String chatUrl){
        this.conversationUrl = conversationUrl;
        this.chatUrl  = chatUrl;
    }

    @Nullable
    public String getConversationUrl(int page){
       return conversationUrl + TextUtils.getPagingParams(MESSAGES_THRESHOLD, page);
    }
    @Nullable
    public String getChatUrl(String conversationId){
        //HERE example of url: https://kakao-makao.com/conversations/@/chat.json  so "@" is a conversationId
        return chatUrl.replace("@",conversationId);
    }



    private AMessenger (){}
    public static void init(String conversationUrl, String chatUrl, final JsonConverter converter){
        if (defaultConfig==null){
            defaultConfig = new AMessenger(conversationUrl, chatUrl) {
                @Override
                public ArrayList<ConversationModel> toConversationModelArray(String json) {
                    return converter.toConversationModelArray(json);
                }
                @Override
                public ChatModel toChatModel(String json) {
                    return converter.toChatModel(json);
                }
                @Override
                public MessageModel toMessageModel(String json) {
                    return converter.toMessageModel(json);
                }
            };
        }
    }
    @Nullable
    public static AMessenger getInstance(){
        if (defaultConfig == null){
            Log.d("A-Messenger", "DefaultConfig is null. Have you initialized the messenger?");
            return null;
        }
        return defaultConfig;
    }

    public abstract ArrayList<ConversationModel> toConversationModelArray(String json);
    public abstract ChatModel toChatModel(String json);
    public abstract MessageModel toMessageModel(String json);

    public interface JsonConverter{
        ArrayList<ConversationModel> toConversationModelArray(String json);
        ChatModel toChatModel(String json);
        MessageModel toMessageModel(String json);
    }
}
