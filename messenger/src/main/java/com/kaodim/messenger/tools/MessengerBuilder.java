package com.kaodim.messenger.tools;

import android.content.Context;
import android.content.Intent;

import com.kaodim.messenger.activities.ConversationsActivity;
import com.kaodim.messenger.models.ConversationModel;

import java.util.ArrayList;

/**
 * Created by Kanskiy on 11/10/2016.
 */

public  class MessengerBuilder {

    Intent intent;
    Context context;
    public MessengerBuilder build(Context context){
        this.context = context;
        intent = new Intent(context, ConversationsActivity.class);
        return this;
    }
    public MessengerBuilder addConversationJsonParser(Class<? extends ArrayList<ConversationModel>> jsonClass){
        intent.putExtra("jsonClass", jsonClass);
        return this;
    }
    public MessengerBuilder addConversationUrl(String conversationUrl){
        intent.putExtra("conversationUrl", conversationUrl);
        return this;
    }


    public void start(){
        context.startActivity(intent);
    }
}
