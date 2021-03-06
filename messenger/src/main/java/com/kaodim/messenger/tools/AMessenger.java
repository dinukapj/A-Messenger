package com.kaodim.messenger.tools;

import android.app.Activity;
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
    public static final int ALL_PAGES = -1;

    private String conversationUrl;
    private String chatUrl;
    private Class parentStackClass;
    public String chatMenuTitle;
    private static AMessenger defaultConfig;
    private Analytics analytics;

    private AMessenger(String conversationUrl, String chatUrl, Class parentStackClass, String chatMenuTitle){
        this.conversationUrl = conversationUrl;
        this.chatUrl  = chatUrl;
        this.parentStackClass = parentStackClass;
        this.chatMenuTitle = chatMenuTitle;
    }

    @Nullable
    public String getConversationUrl(int page){
       return conversationUrl + TextUtils.getPagingParams(MESSAGES_THRESHOLD, page);
    }
    @Nullable
    public String getChatUrl(String conversationId, int page){
        //HERE example of url: https://kakao-makao.com/conversations/@/chat.json  so "@" is a conversationId
        String url = chatUrl.replace("@",conversationId);
        if (page!=ALL_PAGES){
            url  +=  TextUtils.getPagingParams(MESSAGES_THRESHOLD, page);
        }
        return  url;
    }

    public Class getParentStackClass (){
    return parentStackClass;
    }


    private AMessenger (){}
    public static AMessenger init(@NonNull String conversationUrl,@NonNull String chatUrl, @NonNull Class<? extends Activity> parentStackClass ,String chatMenuTitle, @NonNull final JsonConverter converter){
        if (defaultConfig==null){
            defaultConfig = new AMessenger(conversationUrl, chatUrl, parentStackClass, chatMenuTitle) {
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

                @Override
                public void menItemClicked(String chatContextId) {
                    converter.menItemClicked(chatContextId);
                }
            };
        }
        return defaultConfig;
    }

    @Nullable
    public static AMessenger getInstance(){
        if (defaultConfig == null){
            Log.d("A-Messenger", "DefaultConfig is null. Have you initialized the messenger?");
            return null;
        }
        return defaultConfig;
    }
    public void trackMessageSent(Class sourceActivity){
        if (analytics!=null){
            analytics.onMessageSent(sourceActivity);
        }
    }
    public void setAnalytics(Analytics analytics){
        this.analytics = analytics;
    }
    public abstract ArrayList<ConversationModel> toConversationModelArray(String json);
    public abstract ChatModel toChatModel(String json);
    public abstract MessageModel toMessageModel(String json);
    public abstract void menItemClicked(String chatContextId);
    public interface JsonConverter{
        ArrayList<ConversationModel> toConversationModelArray(String json);
        ChatModel toChatModel(String json);
        MessageModel toMessageModel(String json);
        void menItemClicked(String chatContextId);
    }
    public interface Analytics{
        void onMessageSent(Class<? extends Activity> sourceActivity);
    }
}
