package com.kaodim.messenger.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.kaodim.messenger.R;
import com.kaodim.messenger.fragments.ChatFragment;
import com.kaodim.messenger.tools.AMessenger;
import com.kaodim.messenger.tools.Logs;
import com.kaodim.messenger.tools.NotificationManager;

/**
 * Created by Kanskiy on 12/10/2016.
 */

public class ChatActivity extends BaseBackButtonActivity {
    public static final String EXTRA_CONVERSATION_ID = "extra_id";
    public static final String EXTRA_INCOMING_MESSAGE_AVATAR = "extra_incoming_message_avatar";
    public static final String EXTRA_INCOMING_MESSAGE_USER_NAME = "extra_incoming_message_user_name";
    String conversationId;
    String incomingMessageName;
    String incomingMessageAvatar;
    String chatUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        if (AMessenger.getInstance()==null){
            Logs.log(Logs.NOT_INITIALISED);
            return;
        }
        conversationId = getIntent().getStringExtra(EXTRA_CONVERSATION_ID);

        if (android.text.TextUtils.isEmpty(conversationId)){
            Logs.log(Logs.NO_CONVERSATION_ID);
            return;
        }

        chatUrl = AMessenger.getInstance().getChatUrl(conversationId);

        incomingMessageAvatar = getIntent().getStringExtra(EXTRA_INCOMING_MESSAGE_AVATAR);
        if (android.text.TextUtils.isEmpty(incomingMessageAvatar)){
            Logs.log(Logs.NO_AVATAR_PASSED);
        }

        incomingMessageName = getIntent().getStringExtra(EXTRA_INCOMING_MESSAGE_USER_NAME);
        if (android.text.TextUtils.isEmpty(incomingMessageName)){
            Logs.log(Logs.NO_NAME_PASSED);
        }


        if (savedInstanceState == null) {
            ChatFragment.newInstance(conversationId,incomingMessageName,incomingMessageAvatar).commit(getSupportFragmentManager());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (conversationId!=null)
        NotificationManager.removeNotifications(conversationId, getApplicationContext());
    }
}